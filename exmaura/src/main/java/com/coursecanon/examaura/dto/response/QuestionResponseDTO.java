package com.coursecanon.examaura.dto.response;

import com.coursecanon.examaura.dto.supportingdto.*;
import com.coursecanon.examaura.entity.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Suppresses unpopulated dynamic schema parameters from final JSON response
public class QuestionResponseDTO
{

    private UUID id;
    private String questionType;
    private String questionText;
    private String questionImageUrl;
    private String explanation; // Automatically cleared by controller if configuration demands a strict non-practice scope
    private Integer position;

    // -- Polymorphic Data containers(Type-specific Payloads) --
    // For OBJECTIVE and MULTIPLE_CHOICE
    private List<String> options;
    private Object correctAnswer;  // Can be Integer or List<Integer>

    // For YES_NO_GRID
    private List<StatementDTO> statements;

    // For DRAG_MATCH
    private List<MatchPairDTO> matchPairs;

    // For DRAG_CLASSIFY
    private List<CategoryDTO> categories;
    private List<ClassifyItemDTO> classifyItems;

    // For INLINE_DROPDOWN
    private String sentenceTemplate;
    private List<InlineDropdownDTO> inlineDropdowns;

    // For MATCHING_DROPDOWN
    private List<DropdownRowDTO> dropdownRows;
}
