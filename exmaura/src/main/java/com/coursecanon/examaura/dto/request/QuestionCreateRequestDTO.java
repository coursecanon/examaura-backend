package com.coursecanon.examaura.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateRequestDTO {
    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Question type is required")
    private String questionType;

    private String questionImageUrl;

    @NotBlank(message = "Explanation is required")
    private String explanation;

    @NotNull(message = "Position is required")
    private Integer position;

    // Polymorphic fields based on question type
    private List<String> options;
    private Object correctAnswer;
    private Object statements;
    private Object matchPairs;
    private Object categories;
    private Object classifyItems;
    private String sentenceTemplate;
    private Object inlineDropdowns;
    private Object dropdownRows;
}
