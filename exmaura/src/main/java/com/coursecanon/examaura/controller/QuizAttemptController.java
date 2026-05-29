package com.coursecanon.examaura.controller;


//This exposes the engine we just built as a clean, RESTful API that your React frontend can easily consume.
//Since we haven't wired up Spring Security and JWTs yet, I am temporarily accepting the userId as a query parameter for the "start attempt" endpoint.
//Once you add security, we will extract that ID directly from the authentication token so the user never has to send it manually.

import com.coursecanon.examaura.dto.request.QuestionAnswerSubmitDTO;
import com.coursecanon.examaura.dto.request.QuizAttemptStartRequestDTO;
import com.coursecanon.examaura.dto.response.QuizAttemptResponseDTO;
import com.coursecanon.examaura.service.QuizAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptController {

    private final QuizAttemptService attemptService;

    /**
     * Step 1: Start a new quiz attempt.
     * Note: userId is passed as a request parameter for now.
     * Future state -> Extract from SecurityContextHolder (JWT).
     */
    @PostMapping("/start")
    public ResponseEntity<QuizAttemptResponseDTO> startAttempt(
            @RequestParam UUID userId,
            @Valid @RequestBody QuizAttemptStartRequestDTO request) {

        log.info("User {} is starting quiz {} in {} mode", userId, request.getQuizId(), request.getMode());
        QuizAttemptResponseDTO startedAttempt = attemptService.startAttempt(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(startedAttempt);
    }

    /**
     * Step 2: Submit an answer for a specific question during an active attempt.
     * This endpoint is called continuously as the user progresses through the exam.
     */
    @PostMapping("/{attemptId}/answers")
    public ResponseEntity<Map<String, String>> submitAnswer(
            @PathVariable UUID attemptId,
            @Valid @RequestBody QuestionAnswerSubmitDTO request) {

        attemptService.submitAnswer(attemptId, request);

        // Returning a simple success message.
        // If this is "Practice Mode", you could modify the Service to return the correct answer here instead!
        return ResponseEntity.ok(Map.of("message", "Answer recorded successfully"));
    }

    /**
     * Step 3: Finish the exam and trigger the final grading compilation.
     */
    @PostMapping("/{attemptId}/finish")
    public ResponseEntity<QuizAttemptResponseDTO> finishAttempt(@PathVariable UUID attemptId) {

        log.info("Finishing attempt ID: {}", attemptId);
        QuizAttemptResponseDTO finishedAttempt = attemptService.finishAttempt(attemptId);
        return ResponseEntity.ok(finishedAttempt);
    }

    /**
     * Step 4: Retrieve the results of a completed exam.
     */
    @GetMapping("/{attemptId}/summary")
    public ResponseEntity<QuizAttemptResponseDTO> getAttemptSummary(@PathVariable UUID attemptId) {

        return ResponseEntity.ok(attemptService.getAttemptSummary(attemptId));
    }
}
