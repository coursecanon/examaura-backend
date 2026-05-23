package com.coursecanon.examaura.dto.request;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class AttemptSubmitRequestDTO
{
    private UUID quizId;
    private String mode;  // "REAL" or "PRACTICE"
    private ZonedDateTime startedAt;
    private ZonedDateTime completedAt;
    private List<QuestionAnswerRequest> answers;
}
