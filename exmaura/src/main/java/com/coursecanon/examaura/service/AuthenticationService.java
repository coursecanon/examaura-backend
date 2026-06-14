package com.coursecanon.examaura.service;

import com.coursecanon.examaura.dto.request.LoginRequestDTO;
import com.coursecanon.examaura.dto.request.RegisterRequestDto;
import com.coursecanon.examaura.dto.response.AuthenticationResponseDTO;
import com.coursecanon.examaura.entity.RefreshToken;
import com.coursecanon.examaura.entity.User;
import com.coursecanon.examaura.entity.enums.OAuthProvider;
import com.coursecanon.examaura.entity.enums.UserRole;
import com.coursecanon.examaura.repository.UserRepository;
import com.coursecanon.examaura.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    /**
     * Registers a new user, hashes their password, and issues a JWT.
     */
    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email address is already registered.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Securely hash the password
                .userRole(UserRole.VIEWER)
                .oauthProvider(OAuthProvider.LOCAL)
                .avatarUrl(request.getAvatarUrl())
                .oauthId(null)
                .build();

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(
                buildClaims(savedUser),
                savedUser
        );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail());

        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .avatar_url(savedUser.getAvatarUrl())
                .role(savedUser.getUserRole().name())
                .oauthProvider(savedUser.getOauthProvider())
                .build();
    }

    /**
     * Verifies user credentials and returns a valid token if authentication succeeds.
     */
    public AuthenticationResponseDTO authenticate(LoginRequestDTO request) {


        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (user.getOauthProvider() != OAuthProvider.LOCAL){
            throw new IllegalArgumentException("This account uses " + user.getOauthProvider() + " login. Please sign in using " + user.getOauthProvider());
        }

        // 1. Authenticate credentials
        // This line automatically invokes your custom UserDetailsService and PasswordEncoder checks
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Generate Access Token (Short-lived JWT)
        String jwtToken = jwtService.generateToken(
                buildClaims(user),
                user
        );

        // 🚀 3. GENERATE REFRESH TOKEN (Long-lived, stored in DB)
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .refreshToken(refreshTokenEntity.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatar_url(user.getAvatarUrl())
                .fullName(user.getFullName())
                .role(user.getUserRole().name())
                .build();
    }

    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getId());
        claims.put("role", user.getUserRole().name());
        claims.put("tokenVersion", user.getTokenVersion());

        return claims;
    }
}