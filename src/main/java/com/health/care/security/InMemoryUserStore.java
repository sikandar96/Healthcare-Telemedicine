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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Nonnull;

@Service
public class InMemoryUserStore implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserStore.class);

    @Value("${app.default-admin.create:false}")
    private boolean createDefaultAdmin;

    @Value("${app.default-admin.username:}")
    private String defaultAdminUsername;

    @Value("${app.default-admin.password:}")
    private String defaultAdminPassword;

    public InMemoryUserStore(UserRepository userRepository, PasswordEncoder passwordEncoder) {
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
            register(defaultAdminUsername, defaultAdminPassword);
            logger.info("Default admin '{}' created. Disable this in production and rotate the password.", defaultAdminUsername);
        } else {
            logger.debug("Default admin '{}' already exists; skipping creation.", defaultAdminUsername);
        }
    }

    @Override
    public @Nonnull UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        UserDocument userDocument = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = userDocument.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return User.withUsername(userDocument.getUsername())
                .password(userDocument.getPassword())
                .authorities(authorities)
                .build();
    }

    public UserDetails register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User already exists: " + username);
        }

        UserDocument userDocument = new UserDocument(username, passwordEncoder.encode(password), List.of("ROLE_USER"));
        userRepository.save(userDocument);
        return loadUserByUsername(username);
    }
}
