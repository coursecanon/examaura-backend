package com.coursecanon.examaura.entity;


import com.coursecanon.examaura.entity.enums.OAuthProvider;
import com.coursecanon.examaura.entity.enums.QuizDifficulty;
import com.coursecanon.examaura.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_oauth_provider", columnList = "oauth_provider")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "fullname", nullable = false, length = 255)
    private String fullName;

    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "user_role", columnDefinition = "user_role_enum")
    @Builder.Default
    private UserRole userRole = UserRole.VIEWER;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "oauth_provider", nullable = false, columnDefinition = "oauth_provider_enum")
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_id", length = 255)
    private String oauthId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quiz> createdQuizzes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizAttempt> attempts = new ArrayList<>();

    @OneToMany(mappedBy = "createdByUser")
    @Builder.Default
    private List<Category> customCategories = new ArrayList<>();

    // Helper methods
    public void addQuiz(Quiz quiz) {
        createdQuizzes.add(quiz);
        quiz.setCreator(this);
    }

    public void addAttempt(QuizAttempt attempt) {
        attempts.add(attempt);
        attempt.setUser(this);
    }

    // ========================================================================
    // SPRING SECURITY: USER DETAILS IMPLEMENTATION
    // ========================================================================

    /**
     * Translates your custom Role enum into Spring Security's GrantedAuthority.
     * Spring Security expects roles to typically be prefixed with "ROLE_".
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    /**
     * We use email as the unique identifier for logging in.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * The password field is automatically mapped, but we still need to override this.
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    // --- Account Status Flags ---
    // For now, we return 'true' for all of these so users can log in immediately.
    // In the future, you could tie these to database boolean columns (e.g., isBanned, isEmailVerified).

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
