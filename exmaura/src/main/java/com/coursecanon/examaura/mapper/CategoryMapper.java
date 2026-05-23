package com.coursecanon.examaura.mapper;

import com.coursecanon.examaura.dto.request.CategoryCreateRequestDto;
import com.coursecanon.examaura.dto.response.CategoryResponseDto;
import com.coursecanon.examaura.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDto toResponse(Category category){
        if (category == null) return  null;

        CategoryResponseDto dto= new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        return dto;
    }

    public Category toEntity(CategoryCreateRequestDto request){
        if (request == null) return  null;

        Category category=new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        return  category;
    }

    public void updateEntityFromRequest(Category category, CategoryCreateRequestDto request){
        if (category ==  null || request == null) return;

        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
    }
}
