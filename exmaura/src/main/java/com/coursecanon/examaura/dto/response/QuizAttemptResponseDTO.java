package com.coursecanon.examaura.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class QuizAttemptResponseDTO {
    private UUID id;
    private UUID quizId;
    private UUID userId;
    private String mode;
    private BigDecimal score;
    private BigDecimal passedScore;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer timeTakenSeconds;
    private Boolean isPassed; // Maps from your computed entity method
    private Instant startedAt;
    private Instant completedAt;
}