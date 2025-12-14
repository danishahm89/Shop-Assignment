package com.assignment.shop.users.controller;

import com.assignment.shop.users.dto.UserDto;
import com.assignment.shop.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @Operation(
        summary = "Register New User",
        description = "Create a new user account"
    )
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return new ResponseEntity<>(userService.createUser(userDto), HttpStatus.CREATED);
    }
}
