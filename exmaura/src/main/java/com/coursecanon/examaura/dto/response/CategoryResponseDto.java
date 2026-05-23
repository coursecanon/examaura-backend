package com.coursecanon.examaura.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String icon;
}
