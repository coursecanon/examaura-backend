package com.coursecanon.examaura.service;

import com.coursecanon.examaura.entity.RefreshToken;
import com.coursecanon.examaura.repository.RefreshTokenRepository;
import com.coursecanon.examaura.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // Set refresh token expiration (e.g., 7 days)
    private final long REFRESH_TOKEN_EXPIRATION_MS = 604800000L;

    public RefreshToken createRefreshToken(String email) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")))
                .token(UUID.randomUUID().toString()) // Opaque string, not a JWT
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        // 🛡️ Check 1: Is it revoked?
        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked. Please log in again.");
        }

        // 🛡️ Check 2: Is it expired?
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token); // Optional: delete or just leave as expired
            throw new RuntimeException("Refresh token was expired. Please make a new signin request.");
        }

        return token;
    }
}