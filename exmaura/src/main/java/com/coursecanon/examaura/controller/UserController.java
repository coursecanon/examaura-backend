package com.coursecanon.examaura.controller;

import com.coursecanon.examaura.dto.request.UserUpdateRequestDto;
import com.coursecanon.examaura.dto.response.UserResponseDto;
import com.coursecanon.examaura.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserProfile(
            @PathVariable(name = "id") UUID id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequestDto request){
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUserAccount(@PathVariable UUID id){
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User account deleted successfully"));
    }
}
