package com.assignment.shop.security.service;

import com.assignment.shop.security.config.CustomUserDetails;
import com.assignment.shop.security.dto.AuthResponse;
import com.assignment.shop.security.dto.LoginRequest;
import com.assignment.shop.security.helper.JwtHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtHelper.generate(userDetails);

        return new AuthResponse(token, userDetails.getUsername(), userDetails.getUser().getRole().name());
    }
}

