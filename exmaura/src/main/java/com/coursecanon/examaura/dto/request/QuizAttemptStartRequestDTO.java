package com.coursecanon.examaura.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class QuizAttemptStartRequestDTO {
    @NotNull(message = "Quiz ID is required")
    private UUID quizId;

    @NotNull(message = "Attempt mode is required")
    private String mode; // e.g., "PRACTICE", "EXAM"
}
