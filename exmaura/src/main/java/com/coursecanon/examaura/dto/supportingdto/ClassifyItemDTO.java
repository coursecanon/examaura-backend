package com.coursecanon.examaura.dto.supportingdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassifyItemDTO {
    private String id;
    private String text;
    private String correctCategoryId;
}
