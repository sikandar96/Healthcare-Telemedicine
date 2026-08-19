package com.health.care;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.care.dtos.AuthRequest;
import com.health.care.entities.UserDocument;
import com.health.care.repositories.UserRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class JwtAuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        UserDocument admin = new UserDocument("admin", "test-password-hash", List.of("ROLE_ADMIN"));
        when(userRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(admin));
        when(passwordEncoder.matches("admin123", "test-password-hash")).thenReturn(true);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void loginAndAccessProtectedEndpoint() throws Exception {
        AuthRequest request = new AuthRequest("admin", "admin123");

        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(responseBody).get("data").get("token").asText();

        mockMvc.perform(get("/api/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, admin")));
    }

    @Test
    void loginByEmail() throws Exception {
        UserDocument user = new UserDocument("email-user", "test-password-hash", List.of("ROLE_PATIENT"));
        user.setEmail("email-user@example.com");
        when(userRepository.findByUsername("email-user@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByEmailIgnoreCase("email-user@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("email-password", "test-password-hash")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("email-user@example.com", "email-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void loginByMobileNumber() throws Exception {
        UserDocument user = new UserDocument("phone-user", "test-password-hash", List.of("ROLE_PATIENT"));
        user.setPhone("+919876543210");
        when(userRepository.findByUsername("9876543210")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByEmailIgnoreCase("9876543210")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByPhone("9876543210")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByPhoneContaining("9876543210")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("phone-password", "test-password-hash")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("9876543210", "phone-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }
}