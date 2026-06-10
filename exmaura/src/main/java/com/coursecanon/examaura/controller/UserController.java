package com.coursecanon.examaura.controller;

import com.coursecanon.examaura.dto.request.PasswordUpdateDTO;
import com.coursecanon.examaura.dto.request.UserUpdateRequestDto;
import com.coursecanon.examaura.dto.response.UserResponseDto;
import com.coursecanon.examaura.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //get user by userid
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserProfile(
            @PathVariable(name = "id") UUID id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    //update profile nbu userid
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequestDto request){
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    //update user password
    @PutMapping("/profile/password")
    public ResponseEntity<?> updatePassword(
            @Valid @RequestBody PasswordUpdateDTO request,
            Principal principal) {

        // The 'principal.getName()' usually returns the email or username
        // that your JWT filter parsed out of the token.
        // This makes it impossible for User A to update User B's password!
        String authenticatedUserEmail = principal.getName();

        try {
            userService.updatePassword(authenticatedUserEmail, request);
            return ResponseEntity.ok().body("Password updated successfully");
        } catch (IllegalArgumentException e) {
            // Return a 400 Bad Request if the old password was wrong
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Delete user by user id
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUserAccount(@PathVariable UUID id){
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User account deleted successfully"));
    }
}
