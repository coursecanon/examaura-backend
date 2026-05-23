package com.coursecanon.examaura.dto.response;

import com.coursecanon.examaura.dto.supportingdto.CategorySummary;
import com.coursecanon.examaura.dto.supportingdto.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDto {
    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String difficulty;
    private Integer durationMinutes;
    private Integer passingPercentage;
    private Integer totalQuestions;
    private BigDecimal averageScore;
    private Boolean isPublished;
    private Boolean isFeatured;
    private Instant createdAt;
    private Instant updatedAt;

    //Flattened relational summay references to prevent recursive loop issues
    private CategorySummary category;
    private UserSummary creator;

    // Nested collection of transformed child questions
    private List<QuestionResponseDTO> questions;
}
