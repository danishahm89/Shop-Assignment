package com.assignment.shop.users.service;

import com.assignment.shop.users.dto.UserDto;
import com.assignment.shop.users.entity.User;
import com.assignment.shop.users.enums.Role;
import com.assignment.shop.users.mapper.UserMapper;
import com.assignment.shop.users.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setup() {
        userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("$2a$10$hashedPassword");
        userDto.setRole(Role.USER);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$hashedPassword")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create new user successfully")
    void whenCreateUser_thenReturnSavedUser() {
        when(userMapper.mapUserDtoToUser(any(UserDto.class))).thenReturn(user);
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(userMapper.mapUsertoUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepo).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail when username already exists")
    void whenDuplicateUsername_thenThrowException() {
        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(RuntimeException.class);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should create user with encrypted password")
    void whenCreateUser_thenPasswordEncrypted() {
        when(userMapper.mapUserDtoToUser(any(UserDto.class))).thenReturn(user);
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(userMapper.mapUsertoUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertThat(result.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("Should assign USER role by default")
    void whenCreateUserWithoutRole_thenAssignUserRole() {
        UserDto dtoWithoutRole = new UserDto();
        dtoWithoutRole.setUsername("newuser");
        dtoWithoutRole.setPassword("pass");

        User userWithDefaultRole = User.builder()
                .username("newuser")
                .role(Role.USER)
                .build();

        when(userMapper.mapUserDtoToUser(any(UserDto.class))).thenReturn(userWithDefaultRole);
        when(userRepo.save(any(User.class))).thenReturn(userWithDefaultRole);
        when(userMapper.mapUsertoUserDto(any(User.class))).thenReturn(dtoWithoutRole);

        userService.createUser(dtoWithoutRole);

        verify(userRepo).save(argThat(savedUser ->
            savedUser.getRole() != null
        ));
    }
}

