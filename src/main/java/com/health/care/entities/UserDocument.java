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

    private String email;

    private String phone;

    private String resetToken;

    private Instant resetTokenExpiresAt;

    public UserDocument() {
    }

    public UserDocument(String username, String password, List<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }
}
