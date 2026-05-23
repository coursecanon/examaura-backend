package com.coursecanon.examaura.service.impl;

import com.coursecanon.examaura.dto.request.CategoryCreateRequestDto;
import com.coursecanon.examaura.dto.response.CategoryResponseDto;
import com.coursecanon.examaura.entity.Category;
import com.coursecanon.examaura.exception.ResourceNotFoundException;
import com.coursecanon.examaura.mapper.CategoryMapper;
import com.coursecanon.examaura.repository.CategoryRepository;
import com.coursecanon.examaura.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDto> getAllCategories(){
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto getCategoryById(UUID id){
        Category category=categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not foud with ID: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CategoryCreateRequestDto request){
        if (categoryRepository.existsByNameIgnoreCase(request.getName())){
            throw new IllegalArgumentException("A category with the name '" + request.getName() + "' already exists.");
        }

        Category newCategory=categoryMapper.toEntity(request);
        Category savedCategory= categoryRepository.save(newCategory);

        log.info("Created new Category : {}", savedCategory.getName());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(UUID id, CategoryCreateRequestDto request){
        Category existingCategory= categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Category not found with ID: "+ id));

        //Ensure we are not chnging the name to one that already exists on another catwgory
        if (!existingCategory.getName().equalsIgnoreCase(request.getName()) && categoryRepository.existsByNameIgnoreCase(request.getName())){
            throw new IllegalArgumentException("A category with name '" +request.getName() + "' already exists.");
        }
        categoryMapper.updateEntityFromRequest(existingCategory, request);
        Category updatedCategory= categoryRepository.save(existingCategory);
        log.info("Updated category ID: {}",id);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public  void deleteCategory(UUID id){
        if (!categoryRepository.existsById(id)){
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
        log.info("Deleted category ID: {}", id);
    }
}
