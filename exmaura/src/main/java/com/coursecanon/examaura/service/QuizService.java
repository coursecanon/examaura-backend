package com.coursecanon.examaura.service;

import com.coursecanon.examaura.dto.request.QuizCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuizResponseDto;
import com.coursecanon.examaura.dto.response.UserQuizResponseDto;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;


public interface QuizService {

    PaginatedResponse<QuizResponseDto> findAllQuizzes(UUID categoryID, String difficulty, String search, Pageable pageable);
    List<QuizResponseDto> getFeaturedQuizzes(int limit);
    QuizResponseDto getQuizDetails(UUID id, boolean includeQuestions);
    QuizResponseDto createQuiz(QuizCreateRequestDTO requestDTO, UUID creatorId);
    QuizResponseDto updateQuiz(UUID id, QuizCreateRequestDTO request);
    List<UserQuizResponseDto> getLatestQuizzesByCreator(UUID creatorId);
    void deleteQuiz(UUID id);
}
