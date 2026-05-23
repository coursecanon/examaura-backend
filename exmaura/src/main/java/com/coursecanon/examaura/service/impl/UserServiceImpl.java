package com.coursecanon.examaura.service.impl;

import com.coursecanon.examaura.dto.request.UserUpdateRequestDto;
import com.coursecanon.examaura.dto.response.UserResponseDto;
import com.coursecanon.examaura.entity.User;
import com.coursecanon.examaura.exception.ResourceNotFoundException;
import com.coursecanon.examaura.mapper.UserMapper;
import com.coursecanon.examaura.repository.UserRepository;
import com.coursecanon.examaura.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDto getUserById(UUID userId){
        User user= userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    public UserResponseDto getUserByEmail(String email){
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UUID userId, UserUpdateRequestDto request){
        User existingUser= userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: "+ userId));
        //check if email is being changed and if new email is alredy in use
        if (!existingUser.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email is already in use by another account");
        }

        userMapper.updateEntityFromRequest(existingUser, request);
        User updatedUser=userRepository.save(existingUser);
        log.info("User profile updated successfully for ID: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id){
        if (!userRepository.existsById(id)){
            throw new ResourceNotFoundException("User not found with ID: " +id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }
}
