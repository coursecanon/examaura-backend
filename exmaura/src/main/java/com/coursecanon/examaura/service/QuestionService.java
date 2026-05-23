package com.coursecanon.examaura.service;

import com.coursecanon.examaura.dto.request.QuestionCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuestionResponseDTO;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import com.coursecanon.examaura.entity.Question;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface QuestionService {
    PaginatedResponse<QuestionResponseDTO> getQuestionsByQuizId(UUID quizId, Pageable pageable);
    QuestionResponseDTO getQuestionById(UUID questionId);
    QuestionResponseDTO addQuestionToQuiz(UUID quizId, QuestionCreateRequestDTO request);
    QuestionResponseDTO updateQuestion(UUID questionId, QuestionCreateRequestDTO request);
    void deleteQuestion(UUID questionId);
}
