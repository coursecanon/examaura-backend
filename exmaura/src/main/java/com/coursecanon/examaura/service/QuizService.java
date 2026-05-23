package com.coursecanon.examaura.service;

import com.coursecanon.examaura.dto.request.QuizCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuizResponseDto;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import com.coursecanon.examaura.entity.Quiz;
import com.coursecanon.examaura.mapper.QuizMapper;
import com.coursecanon.examaura.repository.QuizRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


public interface QuizService {

    PaginatedResponse<QuizResponseDto> findAllQuizzes(UUID categoryID, String difficulty, String search, Pageable pageable);
    List<QuizResponseDto> getFeaturedQuizzes(int limit);
    QuizResponseDto getQuizDetails(UUID id, boolean includeQuestions);
    QuizResponseDto createQuiz(QuizCreateRequestDTO requestDTO, UUID creatorId);
    QuizResponseDto updateQuiz(UUID id, QuizCreateRequestDTO request);
    void deleteQuiz(UUID id);
}
