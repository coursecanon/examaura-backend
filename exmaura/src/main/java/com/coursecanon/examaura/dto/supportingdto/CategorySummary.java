package com.coursecanon.examaura.dto.supportingdto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategorySummary
{
    private UUID id;
    private String name;
    private  String slug;
}
