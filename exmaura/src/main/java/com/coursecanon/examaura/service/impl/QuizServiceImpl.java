package com.coursecanon.examaura.service.impl;

import com.coursecanon.examaura.dto.request.QuizCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuizResponseDto;
import com.coursecanon.examaura.dto.response.UserQuizResponseDto;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import com.coursecanon.examaura.entity.Category;
import com.coursecanon.examaura.entity.Question;
import com.coursecanon.examaura.entity.Quiz;
import com.coursecanon.examaura.entity.User;
import com.coursecanon.examaura.entity.enums.QuizDifficulty;
import com.coursecanon.examaura.exception.ResourceNotFoundException;
import com.coursecanon.examaura.mapper.QuestionMapper;
import com.coursecanon.examaura.mapper.QuizMapper;
import com.coursecanon.examaura.repository.CategoryRepository;
import com.coursecanon.examaura.repository.QuizRepository;
import com.coursecanon.examaura.repository.UserRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;

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
    public QuizResponseDto createQuiz(QuizCreateRequestDTO request, UUID creatorId) {
        // 1. Map basic fields
        Quiz quiz = quizMapper.toEntity(request, creatorId);

        // 2. FIX FOR NULL CATEGORY/CREATOR:
        // Fetch the full entities from DB instead of just passing references
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with Id: "+ request.getCategoryId()));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: "+ creatorId));

        quiz.setCategory(category);
        quiz.setCreator(creator);

        // 3. FIX FOR MISSING QUESTIONS:
        // Convert DTOs to Entities and link them to the Quiz
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            request.getQuestions().forEach(questionDto -> {
                Question question = questionMapper.toEntity(questionDto);
                // Use the helper method so Hibernate knows they belong to this Quiz!
                quiz.addQuestion(question);
            });

            // Update the total questions count
            quiz.setTotalQuestions(request.getQuestions().size());
        } else {
            quiz.setTotalQuestions(0);
        }

        // 4. Save and return (Hibernate will cascade the save to the questions automatically)
        Quiz savedQuiz = quizRepository.save(quiz);
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

    @Override
    public List<UserQuizResponseDto> getLatestQuizzesByCreator(UUID creatorId) {
        // Utilizes the new optimized repository method we added
        List<Quiz> latestQuizzes = quizRepository.findTop10ByCreatorIdOrderByCreatedAtDesc(creatorId);

        return latestQuizzes.stream()
                .map(quizMapper::minimalQuiz)
                .collect(Collectors.toList());
    }
}
