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

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<JwtAuthenticationResponseDTO> refreshToken(@RequestBody TokenRefreshRequestDTO request) {

        JwtAuthenticationResponseDTO response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }
}