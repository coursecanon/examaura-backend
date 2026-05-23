package com.coursecanon.examaura.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

// Question Result DTO (for detailed results)
public class QuestionResultDTO {
    private UUID questionId;
    private String questionText;
    private String questionType;
    private Boolean isCorrect;
    private Object userAnswer;
    private Object correctAnswer;
    private String explanation;
    private BigDecimal pointsEarned;
    private Integer timeSpentSeconds;
}
