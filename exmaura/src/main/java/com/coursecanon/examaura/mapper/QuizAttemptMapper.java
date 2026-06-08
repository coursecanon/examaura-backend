package com.coursecanon.examaura.mapper;

import com.coursecanon.examaura.dto.response.QuestionAnswerResponseDTO;
import com.coursecanon.examaura.dto.response.QuizAttemptResponseDTO;
import com.coursecanon.examaura.entity.QuizAttempt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        dto.setIsPassed(attempt.isPassed());
        dto.setPassedScore(attempt.getPassedScore());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());

        // 🚀 Map the entity answers to the DTO answers
        if (attempt.getQuestionAnswers() != null) {
            List<QuestionAnswerResponseDTO> answerDTOs = attempt.getQuestionAnswers().stream()
                    .map(answer -> {
                        QuestionAnswerResponseDTO ansDto = new QuestionAnswerResponseDTO();
                        if (answer.getQuestion() != null) {
                            ansDto.setQuestionId(answer.getQuestion().getId());
                        }
                        ansDto.setUserAnswer(answer.getUserAnswer());
                        ansDto.setIsCorrect(answer.getIsCorrect());
                        ansDto.setTimeSpentSeconds(answer.getTimeSpentSeconds());
                        return ansDto;
                    })
                    .collect(Collectors.toList());

            dto.setQuestionAnswers(answerDTOs);
        }

        return dto;
    }
}