package com.coursecanon.examaura.controller;

import com.coursecanon.examaura.dto.request.QuizCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuizResponseDto;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import com.coursecanon.examaura.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    //Endpoint 11: List all the quizes with dynamic sorting, pagination and filters

    @GetMapping
    public ResponseEntity<PaginatedResponse<QuizResponseDto>> listAllQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "ASC") String order){
        Sort sorting=order.equalsIgnoreCase("ASC") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        Pageable pageable= PageRequest.of(page, size, sorting);
        PaginatedResponse<QuizResponseDto> response=quizService.findAllQuizzes(category, difficulty, search, pageable);

        return ResponseEntity.ok(response);
    }

    // Endpoint 12 : Get Quiz Details

    @GetMapping("/{id}")
    public  ResponseEntity<QuizResponseDto> getQuizDetails(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean includeQuestions
    ){
        QuizResponseDto quizResponse= quizService.getQuizDetails(id, includeQuestions);
        return ResponseEntity.ok(quizResponse);
    }

    @PostMapping
    public ResponseEntity<QuizResponseDto> createQuiz(
            @Valid @RequestBody QuizCreateRequestDTO request) {
        UUID currentUserId = UUID.fromString("6bd66326-570b-46f8-a2bc-afef9c7151fe");
        QuizResponseDto quiz = quizService.createQuiz(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    //Endpoint 14: Update Quiz

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateQuiz(
            @PathVariable UUID id,
            @Valid @RequestBody QuizCreateRequestDTO request
    ){
        QuizResponseDto updateQuiz= quizService.updateQuiz(id, request);

        Map<String, Object> responseBody= Map.of(
                "id", updateQuiz.getId(),
                "title", updateQuiz.getTitle(),
                "updatedAt", updateQuiz.getUpdatedAt(),
                "message", "Quiz Updated Successfully"
        );
        return ResponseEntity.ok(responseBody);
    }

    //Endpont 15: Delete Quiz
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteQuiz(@PathVariable UUID id){
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(Map.of("message", "Quiz deleted successfully"));
    }
}
