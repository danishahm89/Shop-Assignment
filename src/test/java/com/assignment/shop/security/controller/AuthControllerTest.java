package com.assignment.shop.security.controller;

import com.assignment.shop.security.dto.AuthResponse;
import com.assignment.shop.security.dto.LoginRequest;
import com.assignment.shop.security.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;


    @Test
    @DisplayName("Should login with valid credentials")
    void whenValidLogin_thenReturnToken() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse();
        response.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        response.setUsername("john");
        response.setRole("USER");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void whenWrongPassword_thenUnauthorized() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrongpass");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return admin role for admin user")
    void whenAdminLogin_thenReturnAdminRole() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        AuthResponse response = new AuthResponse();
        response.setToken("admin.jwt.token");
        response.setUsername("admin");
        response.setRole("ADMIN");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}

