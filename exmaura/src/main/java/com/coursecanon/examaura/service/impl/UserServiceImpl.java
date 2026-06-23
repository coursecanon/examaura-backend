package com.coursecanon.examaura.service.impl;

import com.coursecanon.examaura.dto.request.PasswordUpdateDTO;
import com.coursecanon.examaura.dto.request.UserUpdateRequestDto;
import com.coursecanon.examaura.dto.response.UserResponseDto;
import com.coursecanon.examaura.entity.User;
import com.coursecanon.examaura.exception.ResourceNotFoundException;
import com.coursecanon.examaura.mapper.UserMapper;
import com.coursecanon.examaura.repository.UserRepository;
import com.coursecanon.examaura.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    // Ensure you have PasswordEncoder injected via constructor or @Autowired
    @Autowired
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    @CacheEvict(value = "users", key = "#userEmail")
    public void updatePassword(String userEmail, PasswordUpdateDTO request) {
        // 1. Fetch the user safely from the database using the email extracted from the JWT
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Security Check: Does the provided current password match the database hash?
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("The current password provided is incorrect.");
        }

        // 3. Security Check: Prevent reusing the same password (optional but recommended)
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("The new password cannot be the same as the current password.");
        }

        // 4. Hash the new password and save
        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(hashedNewPassword);
        User verifiedUser = userRepository.findById(user.getId()).get();
        user.incrementTokenVersion();
        userRepository.save(user);
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
