package com.coursecanon.examaura.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserAttemptResponseDto {
    private UUID attemptId;
    private UUID quizId;
    private String quizName;
    private String category;
    private BigDecimal passedScore;         // Percentage or points achieved
    private Instant completedAt;   // Attempted timestamp
}