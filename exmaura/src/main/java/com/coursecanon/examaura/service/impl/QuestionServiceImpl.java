package com.coursecanon.examaura.service.impl;

import com.coursecanon.examaura.dto.request.QuestionCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuestionResponseDTO;
import com.coursecanon.examaura.dto.response.QuizResponseDto;
import com.coursecanon.examaura.dto.supportingdto.PaginatedResponse;
import com.coursecanon.examaura.entity.Question;
import com.coursecanon.examaura.entity.Quiz;
import com.coursecanon.examaura.entity.enums.AttemptMode;
import com.coursecanon.examaura.entity.enums.QuestionType;
import com.coursecanon.examaura.exception.ResourceNotFoundException;
import com.coursecanon.examaura.mapper.QuestionMapper;
import com.coursecanon.examaura.repository.QuestionRepository;
import com.coursecanon.examaura.repository.QuizRepository;
import com.coursecanon.examaura.service.QuestionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private  final QuizRepository quizRepository;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    public PaginatedResponse<QuestionResponseDTO> getQuestionsByQuizId(UUID quizId, Pageable pageable){
        if (!quizRepository.existsById(quizId)){
            throw new ResourceNotFoundException("Quiz not found with Id: " + quizId);
        }
        Page<Question> questionPage=questionRepository.findByQuizId(quizId, pageable);
        Page<QuestionResponseDTO> dtoPage=questionPage.map(questionMapper::toResponse);
        return PaginatedResponse.fromPage(dtoPage);
    }

    @Override
    public QuestionResponseDTO getQuestionById(UUID questionId){
        Question question=questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: "+ questionId));
        return questionMapper.toResponse(question);
    }

    @Override
    @Transactional
    public QuestionResponseDTO addQuestionToQuiz(UUID quizId, QuestionCreateRequestDTO request){
        Quiz quiz=quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        Question question=questionMapper.toEntity(request);
        question.setQuiz(quiz);

        //Dynamically increment the total questions counter on the parent quiz
        quiz.setTotalQuestions(quiz.getTotalQuestions() + 1);
        quizRepository.save(quiz);

        Question savedQuestion= questionRepository.save(question);
        return questionMapper.toResponse(savedQuestion);
    }

@Transactional
public QuestionResponseDTO updateQuestion(UUID questionId, QuestionCreateRequestDTO request) {
    Question existingQuestion = questionRepository.findById(questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));

    // The mapper handles all the dirty work of applying the changes!
    questionMapper.updateEntityFromRequest(existingQuestion, request);

    Question updatedQuestion = questionRepository.save(existingQuestion);
    return questionMapper.toResponse(updatedQuestion);
}

    @Override
    @Transactional
    public void deleteQuestion(UUID questionId){
        Question question=questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: "+ questionId));

        Quiz quiz=question.getQuiz();
        questionRepository.delete(question);

        //Decrement the total questions counter on the parent quiz to keep data synchrorinized
        if (quiz.getTotalQuestions() > 0){
            quiz.setTotalQuestions(quiz.getTotalQuestions() - 1);
        }
    }
}
