package com.health.care.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.health.care.entities.UserDocument;
import com.health.care.repositories.UserRepository;
import com.health.care.services.OtpDeliveryService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.Collection;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Nonnull;

@Service
public class MongoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpDeliveryService otpDeliveryService;
    private static final Logger logger = LoggerFactory.getLogger(MongoUserDetailsService.class);

    @Value("${app.default-admin.create:false}")
    private boolean createDefaultAdmin;

    @Value("${app.default-admin.username:}")
    private String defaultAdminUsername;

    @Value("${app.default-admin.password:}")
    private String defaultAdminPassword;

    public MongoUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder, OtpDeliveryService otpDeliveryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpDeliveryService = otpDeliveryService;
    }

    @PostConstruct
    void initializeDefaultUser() {
        if (!createDefaultAdmin) {
            logger.debug("Default admin creation is disabled (app.default-admin.create=false).");
            return;
        }

        if (defaultAdminUsername == null || defaultAdminUsername.isBlank()
                || defaultAdminPassword == null || defaultAdminPassword.isBlank()) {
            logger.warn("Default admin creation requested but username or password is missing. Skipping creation.");
            return;
        }

        if (!userRepository.existsByUsername(defaultAdminUsername)) {
            register(defaultAdminUsername, defaultAdminPassword, "ROLE_ADMIN");
            logger.info("Default admin '{}' created. Disable this in production and rotate the password.", defaultAdminUsername);
        } else {
            logger.debug("Default admin '{}' already exists; skipping creation.", defaultAdminUsername);
        }
    }

    @Override
    public @Nonnull UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        UserDocument userDocument;
        try {
            userDocument = findByIdentifier(username);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException("User not found: " + username, exception);
        }

        List<String> roles = userDocument.getRoles() == null || userDocument.getRoles().isEmpty()
                ? List.of("ROLE_PATIENT") : userDocument.getRoles();
        roles = roles.stream().map(this::normalizeAuthority).distinct().toList();
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return User.withUsername(userDocument.getUsername())
                .password(userDocument.getPassword())
                .authorities(authorities)
                .build();
    }

    public UserDetails register(String username, String password) {
        return register(username, password, "ROLE_PATIENT");
    }

    public UserDetails register(String username, String password, String requestedRole) {
        return register(username, password, requestedRole, null, null, null);
    }

    public UserDetails register(String username, String password, String requestedRole, String email, String phone) {
        return register(username, password, requestedRole, email, phone, null);
    }

    public UserDetails register(String username, String password, String requestedRole, String email, String phone, String fullName) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User already exists: " + username);
        }
        if (normalizedEmail != null && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered: " + normalizedEmail);
        }
        if (normalizedPhone != null && (userRepository.findByPhone(normalizedPhone).isPresent()
                || findByPhoneDigits(normalizedPhone).isPresent())) {
            throw new IllegalArgumentException("Mobile number is already registered");
        }
        String role = normalizeAuthority(requestedRole == null || requestedRole.isBlank() ? "ROLE_PATIENT" : requestedRole);
        UserDocument userDocument = new UserDocument(username, passwordEncoder.encode(password), List.of(role));
        userDocument.setEmail(normalizedEmail);
        userDocument.setPhone(normalizedPhone);
        userDocument.setFullName(fullName == null || fullName.isBlank() ? username : fullName.trim());
        userDocument.setFullName(fullName == null || fullName.isBlank() ? username : fullName.trim());
        userRepository.save(userDocument);
        return loadUserByUsername(username);
    }

    public String createResetToken(String identifier) {
        UserDocument userDocument = findByIdentifier(identifier);
        String token = UUID.randomUUID().toString();
        userDocument.setResetToken(token);
        userDocument.setResetTokenExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(userDocument);
        return token;
    }

    public OtpDeliveryService.DeliveryResult requestOtp(String identifier, String channel) {
        UserDocument userDocument = findByIdentifier(identifier);
        String otp = String.format("%06d", java.util.concurrent.ThreadLocalRandom.current().nextInt(1_000_000));
        userDocument.setResetOtpHash(passwordEncoder.encode(otp));
        userDocument.setResetOtpExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        userDocument.setResetOtpChannel(channel == null ? "email" : channel.trim().toLowerCase(java.util.Locale.ROOT));
        userDocument.setResetOtpVerified(false);
        userRepository.save(userDocument);
        return otpDeliveryService.deliver(userDocument.getEmail(), userDocument.getPhone(), channel, otp);
    }

    public String verifyOtp(String identifier, String otp) {
        UserDocument userDocument = findByIdentifier(identifier);
        if (userDocument.getResetOtpHash() == null || userDocument.getResetOtpExpiresAt() == null || userDocument.getResetOtpExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP is invalid or expired");
        }
        if (!passwordEncoder.matches(otp, userDocument.getResetOtpHash())) {
            throw new IllegalArgumentException("OTP is invalid or expired");
        }
        userDocument.setResetOtpVerified(true);
        userDocument.setResetToken(UUID.randomUUID().toString());
        userDocument.setResetTokenExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        userRepository.save(userDocument);
        return userDocument.getResetToken();
    }

    public void resetPassword(String token, String password) {
        UserDocument userDocument = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Reset link is invalid or expired"));
        if (userDocument.getResetTokenExpiresAt() == null || userDocument.getResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reset link is invalid or expired");
        }
        if (!userDocument.isResetOtpVerified()) {
            throw new IllegalArgumentException("Verify the OTP before resetting your password");
        }
        userDocument.setPassword(passwordEncoder.encode(password));
        userDocument.setResetToken(null);
        userDocument.setResetTokenExpiresAt(null);
        userDocument.setResetOtpHash(null);
        userDocument.setResetOtpExpiresAt(null);
        userDocument.setResetOtpChannel(null);
        userDocument.setResetOtpVerified(false);
        userRepository.save(userDocument);
    }

    private UserDocument findByIdentifier(String identifier) {
        String normalized = identifier == null ? "" : identifier.trim();
        String normalizedPhone = normalizePhone(normalized);
        String phoneDigits = normalizedPhone == null ? "" : normalizedPhone.replaceAll("\\D", "");
        String lastTenDigits = phoneDigits.length() > 10 ? phoneDigits.substring(phoneDigits.length() - 10) : phoneDigits;
        return userRepository.findByUsername(normalized)
                .or(() -> userRepository.findByEmailIgnoreCase(normalized))
                .or(() -> userRepository.findByPhone(normalized))
                .or(() -> lastTenDigits.isBlank() ? java.util.Optional.empty() : userRepository.findByPhoneContaining(lastTenDigits))
                .orElseThrow(() -> new IllegalArgumentException("No account found for that username, email, or mobile number"));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        return phone.trim().replaceAll("[\\s()-]", "");
    }

    private java.util.Optional<UserDocument> findByPhoneDigits(String phone) {
        String digits = phone.replaceAll("\\D", "");
        String lastTenDigits = digits.length() > 10 ? digits.substring(digits.length() - 10) : digits;
        return lastTenDigits.isBlank() ? java.util.Optional.empty() : userRepository.findByPhoneContaining(lastTenDigits);
    }

    public UserDetails updateRoles(String username, Collection<String> requestedRoles) {
        UserDocument userDocument;
        try {
            userDocument = findByIdentifier(username);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException("User not found: " + username, exception);
        }
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
        List<String> roles = requestedRoles.stream()
                .map(this::normalizeAuthority)
                .distinct()
                .toList();
        userDocument.setRoles(roles);
        userRepository.save(userDocument);
        return loadUserByUsername(username);
    }

    private String normalizeAuthority(String role) {
        String normalized = role == null ? "ROLE_PATIENT" : role.trim().toUpperCase(java.util.Locale.ROOT);
        if ("ROLE_USER".equals(normalized)) {
            return "ROLE_PATIENT";
        }
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}
