package com.assignment.shop.users.mapper;

import com.assignment.shop.users.dto.UserDto;
import com.assignment.shop.users.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    UserDto mapUsertoUserDto(User user);
    User mapUserDtoToUser(UserDto userDto);

}
