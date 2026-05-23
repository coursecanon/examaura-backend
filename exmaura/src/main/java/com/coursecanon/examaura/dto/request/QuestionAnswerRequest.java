package com.coursecanon.examaura.dto.request;

import java.util.UUID;

public class QuestionAnswerRequest {
    private UUID questionId;
    private Object userAnswer;
    private Integer timeSpentSeconds;
}
