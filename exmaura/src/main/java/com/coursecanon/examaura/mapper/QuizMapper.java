package com.coursecanon.examaura.mapper;

import com.coursecanon.examaura.dto.request.QuizCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuizResponseDto;
import com.coursecanon.examaura.dto.supportingdto.CategorySummary;
import com.coursecanon.examaura.dto.supportingdto.UserSummary;
import com.coursecanon.examaura.entity.Category;
import com.coursecanon.examaura.entity.Quiz;
import com.coursecanon.examaura.entity.User;
import com.coursecanon.examaura.entity.enums.QuizDifficulty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuizMapper {
    private final QuestionMapper questionMapper;

    //to update Quiz
    //Map an entity to a formatted output DTO matching frontend interface
    public QuizResponseDto toResponse(Quiz quiz){
        if (quiz == null){
            return null;
        }

        QuizResponseDto response=new QuizResponseDto();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setSlug(quiz.getSlug());
        response.setDescription(quiz.getDescription());
        response.setDifficulty(quiz.getDifficulty().name());
        response.setDurationMinutes(quiz.getDurationMinutes());
        response.setPassingPercentage(quiz.getPassingPercentage());
        response.setTotalQuestions(quiz.getTotalQuestions());
        response.setAverageScore(quiz.getAverageScore());
        response.setIsPublished(quiz.getIsPublished());
        response.setIsFeatured(quiz.getIsFeatured());
        response.setCreatedAt(quiz.getCreatedAt());
        response.setUpdatedAt(quiz.getUpdatedAt());

        //Avoid Infinite recursion by mapping shallow summaries
        if (quiz.getCategory() != null){
            response.setCategory(CategorySummary.builder()
                            .id(quiz.getCategory().getId())
                            .name(quiz.getCategory().getName())
                            .slug(quiz.getCategory().getSlug())
                    .build());
        }

        if (quiz.getCreator() != null){
            response.setCreator(UserSummary.builder()
                            .id(quiz.getCreator().getId())
                            .name(quiz.getCreator().getFullName())
                            .email(quiz.getCreator().getEmail())
                            .avatarUrl(quiz.getCreator().getAvatarUrl())
                    .build());
        }

        //Delegate nested question transformation to specialized mapped collection loop
        if (quiz.getQuestions() != null){
            response.setQuestions(quiz.getQuestions().stream()
                    .map(questionMapper::toResponse)
                    .toList());
        }
        else {
            response.setQuestions(new ArrayList<>());
        }
        return response;
    }

    //Convert inbound registration data into an operational entity instance
    // t o create Quiz
    public Quiz toEntity(QuizCreateRequestDTO request, UUID createrId){
        if (request == null){
            return null;
        }

        //Create references for core databse associations
        Category category= new Category();
        category.setId(request.getCategoryId());


        User creator =new User();
        creator.setId(createrId);

        Quiz quiz= Quiz.builder()
                .title((request.getTitle()))
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .passingPercentage(request.getPassingPercentage())
                .difficulty(QuizDifficulty.fromValue(request.getDifficulty()))
                .category(category)
                .creator(creator)
                .isPublished(true)
                .isFeatured(false)
                .build();

        //enforce entity rules required slug structures before saving
        return quiz;
    }
}
