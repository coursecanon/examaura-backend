package com.coursecanon.examaura.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QuizCreateRequestDTO {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotNull(message = "Difficulty is required")
    private String difficulty;  // "BEGINNER", "INTERMEDIATE", "ADVANCED"

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value= 600, message= "Duration cannot exceed 600 minutes")
    private Integer durationMinutes;

    @Min(value = 0)
    @Max(value = 100)
    private Integer passingPercentage = 70;

    @NotNull(message = "At least one question is required")
    @Size(min = 1, message = "Quiz must have at least one question")
    private List<QuestionCreateRequestDTO> questions;
}
