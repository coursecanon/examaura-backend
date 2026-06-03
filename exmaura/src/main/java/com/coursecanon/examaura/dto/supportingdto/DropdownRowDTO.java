package com.coursecanon.examaura.dto.supportingdto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropdownRowDTO {
    private String id;
    private String label;
    private List<String> options;
    private Integer correctAnswer;
}
