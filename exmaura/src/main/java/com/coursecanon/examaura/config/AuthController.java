//package com.coursecanon.examaura.config;
//
//
//import com.coursecanon.examaura.dto.request.RefreshTokenRequest;
//import com.coursecanon.examaura.dto.response.AuthResponse;
//import com.coursecanon.examaura.dto.response.UserResponse;
//import com.coursecanon.examaura.security.JwtTokenProvider;
//import com.coursecanon.examaura.service.AuthService;
//import com.coursecanon.examaura.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//    private final UserService userService;
//    private final JwtTokenProvider tokenProvider;
//
//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refreshToken(
//            @RequestBody RefreshTokenRequest request) {
//
//        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/me")
//    public ResponseEntity<UserResponse> getCurrentUser(
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        UUID userId = UUID.fromString(userDetails.getUsername());
//        UserResponse user = userService.getUserById(userId);
//        return ResponseEntity.ok(user);
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout() {
//        // With JWT, logout is handled client-side by removing tokens
//        // Optionally implement token blacklist if needed
//        return ResponseEntity.ok().build();
//    }
//}