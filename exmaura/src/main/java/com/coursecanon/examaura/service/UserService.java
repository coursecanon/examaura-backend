package com.coursecanon.examaura.service;

import com.coursecanon.examaura.dto.request.UserUpdateRequestDto;
import com.coursecanon.examaura.dto.response.UserResponseDto;

import java.util.UUID;

public interface UserService {
    UserResponseDto getUserById(UUID userId);
    UserResponseDto getUserByEmail(String email);
    UserResponseDto updateUser(UUID id, UserUpdateRequestDto request);
    void deleteUser(UUID userId);
}
