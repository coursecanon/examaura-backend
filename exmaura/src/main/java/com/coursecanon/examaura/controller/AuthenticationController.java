package com.coursecanon.examaura.controller;

import com.coursecanon.examaura.dto.request.LoginRequestDTO;
import com.coursecanon.examaura.dto.request.RegisterRequestDto;
import com.coursecanon.examaura.dto.request.TokenRefreshRequestDTO;
import com.coursecanon.examaura.dto.response.AuthenticationResponseDTO;
import com.coursecanon.examaura.dto.response.JwtAuthenticationResponseDTO;
import com.coursecanon.examaura.entity.RefreshToken;
import com.coursecanon.examaura.security.JwtService;
import com.coursecanon.examaura.service.AuthenticationService;
import com.coursecanon.examaura.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(
            @Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponseDTO> refreshtoken(@RequestBody TokenRefreshRequestDTO request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(user -> {
                    // Generate a new, short-lived Access Token
                    String newAccessToken = jwtService.generateToken(user);

                    return ResponseEntity.ok(new JwtAuthenticationResponseDTO(
                            newAccessToken,
                            requestRefreshToken, // Send back the same refresh token
                            "Bearer"
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }
}