package com.coursecanon.examaura.service.impl;

import com.coursecanon.examaura.dto.request.QuizCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuizResponseDto;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import com.coursecanon.examaura.entity.Category;
import com.coursecanon.examaura.entity.Quiz;
import com.coursecanon.examaura.entity.enums.QuizDifficulty;
import com.coursecanon.examaura.exception.ResourceNotFoundException;
import com.coursecanon.examaura.mapper.QuizMapper;
import com.coursecanon.examaura.repository.QuizRepository;
import com.coursecanon.examaura.service.QuizService;
import com.coursecanon.examaura.service.helper.QuizSpecifications;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    public PaginatedResponse<QuizResponseDto> findAllQuizzes(UUID categoryId, String difficulty, String search, Pageable pageable){
        Specification<Quiz> spec= QuizSpecifications.filterQuizzes(categoryId, difficulty, search);
        Page<Quiz> quizPage=quizRepository.findAll(spec, pageable);

        //convert Page entity chunks to response DTO wrappers
        Page<QuizResponseDto> dtoPage=quizPage.map(quizMapper::toResponse);

        //Hide questions array from Heavey listing queries to save database execution cost an bandwidth
        dtoPage.forEach(quiz-> quiz.setQuestions(new ArrayList<>()));
        return PaginatedResponse.fromPage(dtoPage);
    }

    public List<QuizResponseDto> getFeaturedQuizzes(int limit){
        //Enforces pagination limit constrains on featured selection
        Pageable limitedPage= PageRequest.of(0, limit);
        List<Quiz> featuredQuizzes= quizRepository.findByIsFeaturedTrueAndIsPublishedTrue();

        return featuredQuizzes.stream()
                .limit(limit)
                .map(quizMapper::toResponse)
                .peek(quiz -> quiz.setQuestions(new ArrayList<>()))
                .toList();
    }

    public QuizResponseDto getQuizDetails(UUID id, boolean includeQuestions){
        Quiz quiz=quizRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Quiz not found with Id: "+ id));

        QuizResponseDto response=quizMapper.toResponse(quiz);
        if (!includeQuestions){
            response.setQuestions(new ArrayList<>());
        }
        return response;
    }

    @Override
    @Transactional
    public QuizResponseDto createQuiz(QuizCreateRequestDTO request, UUID creatorId){
        Quiz quiz=quizMapper.toEntity(request, creatorId);
        Quiz savedQuiz=quizRepository.save(quiz);
        return quizMapper.toResponse(savedQuiz);
    }

    @Override
    @Transactional
    public QuizResponseDto updateQuiz(UUID id, QuizCreateRequestDTO request){
        Quiz existingQuiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: "+ id));
        //Dynamically recalulate fields

        existingQuiz.setTitle(request.getTitle());
        existingQuiz.setDescription(request.getDescription());
        existingQuiz.setDurationMinutes(request.getDurationMinutes());
        existingQuiz.setPassingPercentage(request.getPassingPercentage());
        existingQuiz.setDifficulty(QuizDifficulty.fromValue(request.getDifficulty()));

        //Update basic tracking relationships
        Category category=new Category();
        category.setId(request.getCategoryId());
        existingQuiz.setCategory(category);

        //Regenerate updated safe-url strings if titles changed
        existingQuiz.prepareSlug();

        Quiz updatedQuiz= quizRepository.save(existingQuiz);
        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(UUID id){
        if (!quizRepository.existsById(id)){
            throw new ResourceNotFoundException("Quiz not found with ID: "+id);

        }
        quizRepository.deleteById(id);
    }
}
