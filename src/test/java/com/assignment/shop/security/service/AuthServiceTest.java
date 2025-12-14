package com.assignment.shop.security.service;

import com.assignment.shop.security.config.CustomUserDetails;
import com.assignment.shop.security.dto.AuthResponse;
import com.assignment.shop.security.dto.LoginRequest;
import com.assignment.shop.security.helper.JwtHelper;
import com.assignment.shop.users.entity.User;
import com.assignment.shop.users.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;


    @Mock
    private JwtHelper jwtHelper;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$encoded_password")
                .role(Role.USER)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void whenValidCredentials_thenReturnToken() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtHelper.generate(any(CustomUserDetails.class))).thenReturn("jwt.token.here");

        // when
        AuthResponse response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should fail login with invalid password")
    void whenInvalidPassword_thenThrowException() {
        // given
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtHelper, never()).generate(any());
    }

    @Test
    @DisplayName("Should include user role in response")
    void whenLogin_thenIncludeRole() {
        // given
        User adminUser = User.builder()
                .id(2L)
                .username("admin")
                .password("encoded")
                .role(Role.ADMIN)
                .build();

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("admin");
        adminLogin.setPassword("admin123");

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(adminDetails);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtHelper.generate(any(CustomUserDetails.class))).thenReturn("admin.token");

        // when
        AuthResponse response = authService.login(adminLogin);

        // then
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }
}
