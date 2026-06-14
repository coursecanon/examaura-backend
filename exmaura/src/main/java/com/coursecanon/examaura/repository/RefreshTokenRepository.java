package com.coursecanon.examaura.repository;

import com.coursecanon.examaura.entity.RefreshToken;
import com.coursecanon.examaura.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserInfo(User user); // Useful for "Logout from all devices"
}