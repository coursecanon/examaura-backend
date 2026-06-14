package com.coursecanon.examaura.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import java.util.UUID;

@Data
public class QuestionAnswerResponseDTO {
    private UUID questionId;
    private JsonNode userAnswer; // Crucial: Keeps the JSON structure intact for the UI
    private Boolean isCorrect;
    private Integer timeSpentSeconds;
}