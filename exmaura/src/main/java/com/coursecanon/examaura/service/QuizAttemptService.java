package com.coursecanon.examaura.service;

import com.coursecanon.examaura.dto.request.QuestionAnswerSubmitDTO;
import com.coursecanon.examaura.dto.request.QuizAttemptStartRequestDTO;
import com.coursecanon.examaura.dto.response.QuizAttemptResponseDTO;

import java.util.UUID;

public interface QuizAttemptService {
    QuizAttemptResponseDTO startAttempt(UUID userId, QuizAttemptStartRequestDTO request);

    // Note: The response here could be void, or return the graded correct/incorrect status
    // depending on if they are in Practice Mode where answers are revealed immediately!
    void submitAnswer(UUID attemptId, QuestionAnswerSubmitDTO request);

    QuizAttemptResponseDTO finishAttempt(UUID attemptId);
    QuizAttemptResponseDTO getAttemptSummary(UUID attemptId);
}