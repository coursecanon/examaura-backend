package com.coursecanon.examaura.mapper;

import com.coursecanon.examaura.dto.request.QuestionCreateRequestDTO;
import com.coursecanon.examaura.dto.response.QuestionResponseDTO;
import com.coursecanon.examaura.dto.supportingdto.CategoryDTO;
import com.coursecanon.examaura.dto.supportingdto.ClassifyItemDTO;
import com.coursecanon.examaura.dto.supportingdto.DropdownRowDTO;
import com.coursecanon.examaura.dto.supportingdto.MatchPairDTO;
import com.coursecanon.examaura.entity.Question;
import com.coursecanon.examaura.entity.enums.QuestionType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionMapper {
    private final ObjectMapper objectMapper;

    public Question toEntity(QuestionCreateRequestDTO request){
        if (request == null){
            return  null;
        }

        Question question=new Question();
        // Map standard flat fileds
        question.setQuestionText(request.getQuestionText());
        question.setQuestionImageUrl(request.getQuestionImageUrl());
        question.setExplanation(request.getExplanation());
        question.setPosition(request.getPosition());


        //safely parse QuestionType enum

        if (request.getQuestionType() != null){
            question.setQuestionType(QuestionType.valueOf(request.getQuestionType().toUpperCase()));
        }

        //Map to polymorphic payload fields
        try {
            if (request.getQuestionType() != null){
                switch (request.getQuestionType().toUpperCase()){
                    case "OBJECTIVE":
                    case "MULTIPLE_CHOICE":
                        if (request.getOptions() != null){
                            //coverts the List<String> to Jackson jsonnode or object for hibernate JSONB
                            question.setOptions(objectMapper.convertValue(request.getOptions(), JsonNode.class));
                        }
                        if (request.getCorrectAnswer() != null){
                            question.setCorrectAnswer(objectMapper.convertValue(request.getCorrectAnswer(), JsonNode.class));
                        }
                        break;
                    case "YES_NO_GRID":
                        if (request.getMatchPairs() != null){
                            //Note: Mapping request.getMatchPairs() back to entity's 'statements' property
                            // to mirror toResponse logic
                            question.setStatements(objectMapper.convertValue(request.getMatchPairs(), JsonNode.class));
                        }
                        break;
                    case "DRAG_CLASSIFY":
                        if (request.getCategories() != null){
                            question.setCategories(objectMapper.convertValue(request.getCategories(), JsonNode.class));
                        }
                        if (request.getClassifyItems() != null){
                            question.setCategories(objectMapper.convertValue(request.getClassifyItems(), JsonNode.class));
                        }
                        break;
                    case "MATCHING_DROPDOWN":
                        if (request.getDropdownRows() != null){
                            question.setCategories(objectMapper.convertValue(request.getDropdownRows(), JsonNode.class));
                        }
                        break;


                }
            }
        } catch (IllegalArgumentException e){
            log.error("Failed to map polymorphic JSONB structure for incoming request: {}", request.getQuestionText(), e);
        }
        return question;
    }

    public QuestionResponseDTO toResponse(Question question){
        if (question== null){
            return null;
        }
        QuestionResponseDTO response =new QuestionResponseDTO();
        response.setId(question.getId());
        response.setQuestionType(question.getQuestionType().name());
        response.setQuestionText(question.getQuestionText());
        response.setQuestionImageUrl(question.getQuestionImageUrl());
        response.setExplanation(question.getExplanation());
        response.setPosition(question.getPosition());

        try {
            switch (question.getQuestionType()){
                case OBJECTIVE:
                case MULTIPLE_CHOICE:
                    if (question.getOptions() != null){
                        response.setOptions(objectMapper.convertValue(question.getOptions(), new TypeReference<List<String>>() {}));
                    }
                    if (question.getCorrectAnswer()!= null){
                        response.setCorrectAnswer(question.getCorrectAnswer());
                    }
                    break;
                case YES_NO_GRID:
                    if (question.getMatchPairs() != null){
                        response.setMatchPairs(objectMapper.convertValue(question.getStatements(), new TypeReference<List<MatchPairDTO>>() {}));
                    }
                    break;
                case DRAG_CLASSIFY:
                    if (question.getCategories() != null){
                        response.setCategories(objectMapper.convertValue(question.getCategories(), new TypeReference<List<CategoryDTO>>() {}));
                    }
                    if (question.getClassifyItems() != null){
                        response.setClassifyItems(objectMapper.convertValue(question.getClassifyItems(), new TypeReference<List<ClassifyItemDTO>>() {}));
                    }
                    break;

                case MATCHING_DROPDOWN:
                    if (question.getDropdownRows() != null){
                        response.setDropdownRows(objectMapper.convertValue(question.getDropdownRows(), new TypeReference<List<DropdownRowDTO>>() {}));
                    }
                    break;
            }
        } catch (IllegalArgumentException e){
            log.error("Failed to map polymorphic JSONB structure for question ID: " + question.getId(), e);
        }
        return response;
    }

    public void updateEntityFromRequest(Question existingQuestion, QuestionCreateRequestDTO request) {
        if (request == null || existingQuestion == null) return;

        existingQuestion.setQuestionText(request.getQuestionText());
        existingQuestion.setQuestionImageUrl(request.getQuestionImageUrl());
        existingQuestion.setExplanation(request.getExplanation());
        existingQuestion.setPosition(request.getPosition());

        if (request.getQuestionType() != null) {
            // existingQuestion.setQuestionType(QuestionType.valueOf(request.getQuestionType().toUpperCase()));
        }

        try {
            if (request.getQuestionType() != null) {
                switch (request.getQuestionType().toUpperCase()) {
                    case "OBJECTIVE":
                    case "MULTIPLE_CHOICE":
                        existingQuestion.setOptions(request.getOptions() != null ?
                                objectMapper.convertValue(request.getOptions(), JsonNode.class) : null);
                        existingQuestion.setCorrectAnswer(request.getCorrectAnswer() != null ?
                                objectMapper.convertValue(request.getCorrectAnswer(), JsonNode.class) : null);
                        break;
                    case "YES_NO_GRID":
                        existingQuestion.setStatements(request.getMatchPairs() != null ?
                                objectMapper.convertValue(request.getMatchPairs(), JsonNode.class) : null);
                        break;
                    case "DRAG_CLASSIFY":
                        existingQuestion.setCategories(request.getCategories() != null ?
                                objectMapper.convertValue(request.getCategories(), JsonNode.class) : null);
                        existingQuestion.setClassifyItems(request.getClassifyItems() != null ?
                                objectMapper.convertValue(request.getClassifyItems(), JsonNode.class) : null);
                        break;
                    case "MATCHING_DROPDOWN":
                        existingQuestion.setDropdownRows(request.getDropdownRows() != null ?
                                objectMapper.convertValue(request.getDropdownRows(), JsonNode.class) : null);
                        break;
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse JSONB payload updates for question ID: {}", existingQuestion.getId(), e);
        }
    }


}
