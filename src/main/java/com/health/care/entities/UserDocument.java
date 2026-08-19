package com.health.care.entities;

import java.time.Instant;
import java.util.List;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private List<String> roles = List.of("ROLE_PATIENT");

    @Indexed(unique = true, sparse = true)
    private String email;

    @Indexed(unique = true, sparse = true)
    private String phone;

    private String fullName;

    private String resetToken;

    private Instant resetTokenExpiresAt;

    private String resetOtpHash;

    private Instant resetOtpExpiresAt;

    private String resetOtpChannel;

    private boolean resetOtpVerified;

    public UserDocument() {
    }

    public UserDocument(String username, String password, List<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }
}
