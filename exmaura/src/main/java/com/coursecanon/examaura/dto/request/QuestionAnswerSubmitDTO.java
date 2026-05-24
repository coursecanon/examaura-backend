package com.coursecanon.examaura.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class QuestionAnswerSubmitDTO {
    @NotNull(message = "Question ID is required")
    private UUID questionId;

    @NotNull(message = "Answer payload cannot be null")
    private Object userAnswer; // Accepts any JSON array/object from frontend

    private Integer timeSpentSeconds = 0;

    private Boolean isAnswerRevealed = false;
}