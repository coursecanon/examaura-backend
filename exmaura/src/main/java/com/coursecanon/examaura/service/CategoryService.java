package com.coursecanon.examaura.service;

import com.coursecanon.examaura.dto.request.CategoryCreateRequestDto;
import com.coursecanon.examaura.dto.response.CategoryResponseDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService
{
    List<CategoryResponseDto> getAllCategories();
    CategoryResponseDto getCategoryById(UUID id);
    CategoryResponseDto createCategory(CategoryCreateRequestDto requestDto);
    CategoryResponseDto updateCategory(UUID id, CategoryCreateRequestDto request);
    void deleteCategory(UUID id);
}
