// ============================================
// QuizMaster Entity Classes
// Java 17+ with Spring Boot 3.2+ and JPA
// ============================================

package com.quizmaster.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.quizmaster.entity.enums.*;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// ============================================
// ENUMS
// ============================================

package com.quizmaster.entity.enums;

public enum OAuthProvider {
    GOOGLE,
    GITHUB,
    LOCAL
}

public enum QuestionType {
    OBJECTIVE,
    MULTIPLE_CHOICE,
    YES_NO_GRID,
    DRAG_MATCH,
    DRAG_CLASSIFY,
    INLINE_DROPDOWN,
    MATCHING_DROPDOWN
}

public enum QuizDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

public enum AttemptMode {
    REAL,
    PRACTICE
}

// ============================================
// USER ENTITY
// ============================================

package com.quizmaster.entity;

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
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
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
    private ZonedDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

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
}

// ============================================
// CATEGORY ENTITY
// ============================================

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_slug", columnList = "slug", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "is_custom")
    @Builder.Default
    private Boolean isCustom = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Quiz> quizzes = new ArrayList<>();

    // Helper method to generate slug from name
    public void generateSlug() {
        if (this.name != null) {
            this.slug = this.name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        }
    }
}

// ============================================
// QUIZ ENTITY
// ============================================

@Entity
@Table(name = "quizzes", indexes = {
    @Index(name = "idx_quizzes_category", columnList = "category_id"),
    @Index(name = "idx_quizzes_creator", columnList = "creator_id"),
    @Index(name = "idx_quizzes_slug", columnList = "slug", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", columnDefinition = "quiz_difficulty_enum")
    @Builder.Default
    private QuizDifficulty difficulty = QuizDifficulty.INTERMEDIATE;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "passing_percentage")
    @Builder.Default
    private Integer passingPercentage = 70;

    @Column(name = "total_questions")
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "total_attempts")
    @Builder.Default
    private Integer totalAttempts = 0;

    @Column(name = "average_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal averageScore = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "published_at")
    private ZonedDateTime publishedAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    @Builder.Default
    private List<QuizAttempt> attempts = new ArrayList<>();

    // Helper methods
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
        this.totalQuestions = questions.size();
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
        this.totalQuestions = questions.size();
    }

    public void generateSlug() {
        if (this.title != null) {
            this.slug = this.title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        }
    }
}

// ============================================
// QUESTION ENTITY
// ============================================

@Entity
@Table(name = "questions", indexes = {
    @Index(name = "idx_questions_quiz_id", columnList = "quiz_id"),
    @Index(name = "idx_questions_type", columnList = "question_type")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, columnDefinition = "question_type_enum")
    private QuestionType questionType;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_image_url", length = 512)
    private String questionImageUrl;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "position", nullable = false)
    private Integer position;

    // JSONB columns for polymorphic data
    @Type(JsonType.class)
    @Column(name = "options", columnDefinition = "jsonb")
    private JsonNode options;

    @Type(JsonType.class)
    @Column(name = "correct_answer", nullable = false, columnDefinition = "jsonb")
    private JsonNode correctAnswer;

    @Type(JsonType.class)
    @Column(name = "statements", columnDefinition = "jsonb")
    private JsonNode statements;

    @Type(JsonType.class)
    @Column(name = "match_pairs", columnDefinition = "jsonb")
    private JsonNode matchPairs;

    @Type(JsonType.class)
    @Column(name = "categories", columnDefinition = "jsonb")
    private JsonNode categories;

    @Type(JsonType.class)
    @Column(name = "classify_items", columnDefinition = "jsonb")
    private JsonNode classifyItems;

    @Column(name = "sentence_template", columnDefinition = "TEXT")
    private String sentenceTemplate;

    @Type(JsonType.class)
    @Column(name = "inline_dropdowns", columnDefinition = "jsonb")
    private JsonNode inlineDropdowns;

    @Type(JsonType.class)
    @Column(name = "dropdown_rows", columnDefinition = "jsonb")
    private JsonNode dropdownRows;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Builder.Default
    private List<QuestionAnswer> answers = new ArrayList<>();
}

// ============================================
// QUIZ_ATTEMPT ENTITY
// ============================================

@Entity
@Table(name = "quiz_attempts", indexes = {
    @Index(name = "idx_attempts_user", columnList = "user_id"),
    @Index(name = "idx_attempts_quiz", columnList = "quiz_id"),
    @Index(name = "idx_attempts_completed_at", columnList = "completed_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private AttemptMode mode;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "time_taken_seconds", nullable = false)
    private Integer timeTakenSeconds;

    @Column(name = "passing_score", nullable = false)
    private Integer passingScore;

    @Column(name = "started_at", nullable = false)
    private ZonedDateTime startedAt;

    @Column(name = "completed_at", nullable = false)
    private ZonedDateTime completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuestionAnswer> questionAnswers = new ArrayList<>();

    // Computed field
    @Transient
    public Boolean isPassed() {
        return score.compareTo(BigDecimal.valueOf(passingScore)) >= 0;
    }

    // Helper methods
    public void addQuestionAnswer(QuestionAnswer answer) {
        questionAnswers.add(answer);
        answer.setAttempt(this);
    }
}

// ============================================
// QUESTION_ANSWER ENTITY
// ============================================

@Entity
@Table(name = "question_answers", indexes = {
    @Index(name = "idx_answers_attempt", columnList = "attempt_id"),
    @Index(name = "idx_answers_question", columnList = "question_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswer {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Type(JsonType.class)
    @Column(name = "user_answer", nullable = false, columnDefinition = "jsonb")
    private JsonNode userAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "points_earned", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pointsEarned = BigDecimal.ZERO;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}

// ============================================
// ADDITIONAL DEPENDENCIES NEEDED IN pom.xml
// ============================================

/*
<dependencies>
    <!-- Hibernate Types for JSONB support -->
    <dependency>
        <groupId>io.hypersistence</groupId>
        <artifactId>hypersistence-utils-hibernate-63</artifactId>
        <version>3.7.0</version>
    </dependency>

    <!-- Lombok for boilerplate reduction -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
*/

// ============================================
// APPLICATION.YML CONFIGURATION
// ============================================

/*
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quizmaster
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for schema management
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          lob:
            non_contextual_creation: true
    show-sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
*/

// ============================================
// END OF ENTITY CLASSES
// ============================================
