package com.coursecanon.examaura.dto.response;

import com.coursecanon.examaura.dto.supportingdto.CategorySummary;
import lombok.Data;

import java.util.UUID;

@Data
public class UserQuizResponseDto {
    private UUID id;
    private String title;
    private String difficulty;
    private Integer durationMinutes;
    private Integer passingPercentage;
    private Integer totalQuestions;
    private String category;
}
