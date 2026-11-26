package com.benkih.estore.product.service;

import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.product.dto.request.CategoryRequest;
import com.benkih.estore.product.dto.response.CategoryResponseDto;
import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDto addCategory(CategoryRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Category name is required.");
        }

        String name = request.getName().trim();
        Category parent = resolveParent(request.getParentSlug());

        boolean exists =
                parent == null
                        ? categoryRepository.existsByNameAndParentIsNull(name)
                        : categoryRepository.existsByNameAndParent(name, parent);

        if (exists) {
            throw new AlreadyExistsException(name + " already exists.");
        }

        Category category = new Category();
        category.setName(name);
        category.setParent(parent);
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryResponseDto getCategoryBySlug(String slug) {
        return toResponse(findBySlug(slug));
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryResponseDto getCategoryByName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Category name is required.");
        }

        return toResponse(
                categoryRepository
                        .findByNameAndParentIsNull(name.trim())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found!")));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findByParentIsNull().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponseDto> getChildCategories(String parentSlug) {
        return categoryRepository.findByParent(findBySlug(parentSlug)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponseDto updateCategory(CategoryRequest request, String slug) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Category name is required.");
        }

        Category category = findBySlug(slug);
        String newName = request.getName().trim();
        Category parent = category.getParent();

        if (!category.getName().equalsIgnoreCase(newName)) {
            boolean exists =
                    parent == null
                            ? categoryRepository.existsByNameAndParentIsNull(newName)
                            : categoryRepository.existsByNameAndParent(newName, parent);

            if (exists) {
                throw new AlreadyExistsException(newName + " already exists.");
            }
        }

        category.setName(newName);
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategoryBySlug(String slug) {
        Category category = findBySlug(slug);
        category.setActive(false);
        categoryRepository.save(category);
    }

    private Category findBySlug(String slug) {
        return categoryRepository
                .findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
    }

    private Category resolveParent(String parentSlug) {
        return parentSlug == null || parentSlug.isBlank() ? null : findBySlug(parentSlug.trim());
    }

    private CategoryResponseDto toResponse(Category category) {
        return new CategoryResponseDto(
                category.getSlug(),
                category.getName(),
                category.getParent() == null ? null : category.getParent().getSlug(),
                category.getDescription(),
                category.getImageUrl(),
                category.getDisplayOrder(),
                category.isActive());
    }
}
