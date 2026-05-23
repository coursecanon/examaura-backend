package com.coursecanon.examaura.controller;

import com.coursecanon.examaura.dto.request.QuestionCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuestionResponseDTO;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import com.coursecanon.examaura.entity.Question;
import com.coursecanon.examaura.entity.enums.AttemptMode;
import com.coursecanon.examaura.mapper.QuestionMapper;
import com.coursecanon.examaura.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor //automatically injects the service and mapper via constructor
public class QuestionController {

    private final QuestionService questionService;

    //get all questions for a specific quiz(Paginated)
    @GetMapping("quizzes/{quizId}/questions")
    public ResponseEntity<PaginatedResponse<QuestionResponseDTO>> getQuestionsByQuiz(
            @PathVariable UUID quizId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order){
        Sort sorting=order.equalsIgnoreCase("DESC") ? Sort.by(sort).descending() : Sort.by(sort).ascending();
        Pageable pageable= PageRequest.of(page, size, sorting);
        return ResponseEntity.ok(questionService.getQuestionsByQuizId(quizId, pageable));
    }

    //Get a specific question by its ID

    @GetMapping("questions/{questionId}")
    public ResponseEntity<QuestionResponseDTO> getQuestionDetails(@PathVariable UUID questionId){
        return ResponseEntity.ok(questionService.getQuestionById(questionId));
    }

    //Add a new questions to a quiz
    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<QuestionResponseDTO> addQuestions(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuestionCreateRequestDTO request){
        QuestionResponseDTO createdQuestion=questionService.addQuestionToQuiz(quizId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    //Update Question
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionCreateRequestDTO request){
        return ResponseEntity.ok(questionService.updateQuestion(questionId, request));
    }


}