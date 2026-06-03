package com.coursecanon.examaura.dto.supportingdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPairDTO {
    private String id;
    private String term;
    private String definition;
    private String correctAnswer;
}
