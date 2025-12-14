package com.assignment.shop.users.service;

import com.assignment.shop.users.dto.UserDto;
import com.assignment.shop.users.mapper.UserMapper;
import com.assignment.shop.users.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto createUser(UserDto userDto) {
        var user = userMapper.mapUserDtoToUser(userDto);
        var savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return userMapper.mapUsertoUserDto(savedUser);
    }
}
