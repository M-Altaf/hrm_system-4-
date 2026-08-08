package com.example.hrm.system.serviceImpl;


import com.example.hrm.system.dtos.requestdto.CategoryRequestDto;
import com.example.hrm.system.dtos.responsedto.CategoryResponseDto;
import com.example.hrm.system.entity.Category;
import com.example.hrm.system.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public CategoryResponseDto getCategoryById(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toDto(cat);
    }

    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Category '" + dto.getName() + "' already exists");
        }
        Category cat = new Category();
        cat.setName(dto.getName());
        cat.setDescription(dto.getDescription());
        return toDto(categoryRepository.save(cat));
    }

    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        cat.setName(dto.getName());
        cat.setDescription(dto.getDescription());
        return toDto(categoryRepository.save(cat));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponseDto toDto(Category cat) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(cat.getId());
        dto.setName(cat.getName());
        dto.setDescription(cat.getDescription());
        return dto;
    }
}