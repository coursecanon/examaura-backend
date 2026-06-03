package com.coursecanon.examaura.entity;

import com.coursecanon.examaura.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Builder.Default
    @Column(name = "passed_score", nullable = false)
    private BigDecimal passedScore = BigDecimal.ZERO;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuestionAnswer> questionAnswers = new ArrayList<>();

    // Computed field
    @Transient
    public Boolean isPassed() {
        return passedScore.compareTo(BigDecimal.valueOf(passingScore)) >= 0;
    }

    // Helper methods
    public void addQuestionAnswer(QuestionAnswer answer) {
        questionAnswers.add(answer);
        answer.setAttempt(this);
    }
}
