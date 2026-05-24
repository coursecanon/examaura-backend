package com.coursecanon.examaura.service.impl;

import com.coursecanon.examaura.dto.request.QuestionAnswerSubmitDTO;
import com.coursecanon.examaura.dto.request.QuizAttemptStartRequestDTO;
import com.coursecanon.examaura.dto.response.QuizAttemptResponseDTO;
import com.coursecanon.examaura.entity.*;
import com.coursecanon.examaura.entity.enums.AttemptMode;
import com.coursecanon.examaura.exception.ResourceNotFoundException;
import com.coursecanon.examaura.mapper.QuizAttemptMapper;
import com.coursecanon.examaura.repository.*;
import com.coursecanon.examaura.service.AnswerEvaluator;
import com.coursecanon.examaura.service.QuizAttemptService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final QuestionAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptMapper attemptMapper;
    private final ObjectMapper objectMapper;
    private final AnswerEvaluator answerEvaluator;

    @Override
    @Transactional
    public QuizAttemptResponseDTO startAttempt(UUID userId, QuizAttemptStartRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .mode(AttemptMode.valueOf(request.getMode().toUpperCase()))
                .score(BigDecimal.ZERO)
                // Assuming your Quiz entity has a way to count its questions or stores the total
                .totalQuestions(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)
                .correctAnswers(0)
                .timeTakenSeconds(0)
                // Assuming your Quiz entity stores what the passing threshold is
                .passingScore(quiz.getPassingPercentage() != null ? quiz.getPassingPercentage() : 70)
                .startedAt(Instant.now())
                .completedAt(null)
                .build();

        QuizAttempt savedAttempt = attemptRepository.save(attempt);
        return attemptMapper.toResponse(savedAttempt);
    }

    @Override
    @Transactional
    public void submitAnswer(UUID attemptId, QuestionAnswerSubmitDTO request) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        // Optional: Block submissions if attempt is already completed
        if (attempt.getCompletedAt() != null) {
            throw new IllegalStateException("Cannot submit answers to a completed exam.");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // 1. Evaluate the incoming answer
        JsonNode userSubmittedJson = objectMapper.convertValue(request.getUserAnswer(), JsonNode.class);
        boolean isCorrect = answerEvaluator.evaluate(
                question.getQuestionType(),
                question.getCorrectAnswer(),
                userSubmittedJson
        );

        // Assign points (Assuming correct is 1.0 points, incorrect is 0.0)
        // If your Question entity has a 'points' field, you can do: isCorrect ? question.getPoints() : BigDecimal.ZERO
        BigDecimal points = isCorrect ? BigDecimal.ONE : BigDecimal.ZERO; // Adjust if questions have custom weights

        // 2. Check if the user has already answered this question
        answerRepository.findByAttemptIdAndQuestionId(attemptId, question.getId())
                .ifPresentOrElse(
                        existingAnswer -> {
                            // SCENARIO 2 LOGIC: Block updates if Practice Mode AND answer was previously revealed
                            if (attempt.getMode() == AttemptMode.PRACTICE && Boolean.TRUE.equals(existingAnswer.getIsAnswerRevealed())) {
                                throw new IllegalStateException("Cannot modify answer after revealing the solution in Practice Mode.");
                            }

                            // SCENARIO 1 & 2 (Unrevealed): Update the existing answer
                            existingAnswer.setUserAnswer(userSubmittedJson);
                            existingAnswer.setIsCorrect(isCorrect);
                            existingAnswer.setPointsEarned(points);
                            // Accumulate time spent if they return to the question
                            existingAnswer.setTimeSpentSeconds(existingAnswer.getTimeSpentSeconds() + request.getTimeSpentSeconds());

                            // If they are revealing it NOW, lock it for the future
                            if (Boolean.TRUE.equals(request.getIsAnswerRevealed())) {
                                existingAnswer.setIsAnswerRevealed(true);
                            }

                            answerRepository.save(existingAnswer);
                        },
                        () -> {
                            // BRAND NEW ANSWER: Insert it into the database
                            QuestionAnswer newAnswer = QuestionAnswer.builder()
                                    .attempt(attempt)
                                    .question(question)
                                    .userAnswer(userSubmittedJson)
                                    .isCorrect(isCorrect)
                                    .pointsEarned(points)
                                    .timeSpentSeconds(request.getTimeSpentSeconds())
                                    .isAnswerRevealed(request.getIsAnswerRevealed() != null ? request.getIsAnswerRevealed() : false)
                                    .build();

                            answerRepository.save(newAnswer);
                        }
                );
    }

    @Override
    @Transactional
    public QuizAttemptResponseDTO finishAttempt(UUID attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        // 1. Tally up correct answers and time
        int totalCorrect = 0;
        int totalTime = 0;
        BigDecimal totalScore = BigDecimal.ZERO;

        for (QuestionAnswer answer : attempt.getQuestionAnswers()) {
            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                totalCorrect++;
                totalScore = totalScore.add(answer.getPointsEarned());
            }
            if (answer.getTimeSpentSeconds() != null) {
                totalTime += answer.getTimeSpentSeconds();
            }
        }

        // 2. Update Attempt Record
        attempt.setCorrectAnswers(totalCorrect);
        attempt.setTimeTakenSeconds(totalTime);
        attempt.setScore(totalScore);
        attempt.setCompletedAt(Instant.now());

        QuizAttempt finishedAttempt = attemptRepository.save(attempt);
        return attemptMapper.toResponse(finishedAttempt);
    }

    @Override
    public QuizAttemptResponseDTO getAttemptSummary(UUID attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        return attemptMapper.toResponse(attempt);
    }

//    Why this architecture is robust
//    Separation of Concerns: Your database service doesn't care how a question is graded, it just asks the AnswerEvaluator for a true/false result.
//
//    Order-Agnostic Validation: By converting JSON Arrays into Java Set objects behind the scenes, you ensure users are not penalized for clicking correct multiple-choice options in a different order than the admin created them.
//
//    Extensible: If you ever add a new question type (e.g., FILL_IN_THE_BLANK or HOTSPOT), you only have to add one method to the AnswerEvaluator class, without ever touching the core attempt service.
}