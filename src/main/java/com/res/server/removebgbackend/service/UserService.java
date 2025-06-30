package com.res.server.removebgbackend.service;

import com.res.server.removebgbackend.dto.UserDto;

public interface UserService {

    UserDto saveUser(UserDto userDto);

    UserDto getUserByClerkId(String clerkId);
    void deleteUserByClerkId(String clerkId);
}
