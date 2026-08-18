package com.health.care.security;

import java.util.List;

import com.health.care.entities.UserDocument;
import com.health.care.repositories.UserRepository;
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
    private static final Logger logger = LoggerFactory.getLogger(MongoUserDetailsService.class);

    @Value("${app.default-admin.create:false}")
    private boolean createDefaultAdmin;

    @Value("${app.default-admin.username:}")
    private String defaultAdminUsername;

    @Value("${app.default-admin.password:}")
    private String defaultAdminPassword;

    public MongoUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        UserDocument userDocument = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

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
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User already exists: " + username);
        }
        String role = normalizeAuthority(requestedRole == null || requestedRole.isBlank() ? "ROLE_PATIENT" : requestedRole);
        UserDocument userDocument = new UserDocument(username, passwordEncoder.encode(password), List.of(role));
        userRepository.save(userDocument);
        return loadUserByUsername(username);
    }

    public UserDetails updateRoles(String username, Collection<String> requestedRoles) {
        UserDocument userDocument = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
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
