package com.coursecanon.examaura.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class AttemptResponseDTO
{
    private UUID id;
    private UUID quizId;
    private String quizTitle;
    private String categoryName;
    private UUID userId;
    private String userName;
    private String mode;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer timeTakenSeconds;
    private Integer passingScore;
    private Boolean isPassed;
    private ZonedDateTime completedAt;
    private List<QuestionResultDTO> questionResults;
}
