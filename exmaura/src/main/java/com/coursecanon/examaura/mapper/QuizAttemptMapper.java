package com.coursecanon.examaura.mapper;

import com.coursecanon.examaura.dto.response.QuizAttemptResponseDTO;
import com.coursecanon.examaura.entity.QuizAttempt;
import org.springframework.stereotype.Component;

@Component
public class QuizAttemptMapper {

    public QuizAttemptResponseDTO toResponse(QuizAttempt attempt) {
        if (attempt == null) return null;

        QuizAttemptResponseDTO dto = new QuizAttemptResponseDTO();
        dto.setId(attempt.getId());

        if (attempt.getQuiz() != null) dto.setQuizId(attempt.getQuiz().getId());
        if (attempt.getUser() != null) dto.setUserId(attempt.getUser().getId());

        if (attempt.getMode() != null) dto.setMode(attempt.getMode().name());

        dto.setScore(attempt.getScore());
        dto.setTotalQuestions(attempt.getTotalQuestions());
        dto.setCorrectAnswers(attempt.getCorrectAnswers());
        dto.setTimeTakenSeconds(attempt.getTimeTakenSeconds());

        // Use the computed boolean from your entity
        dto.setIsPassed(attempt.isPassed());

        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());

        return dto;
    }
}