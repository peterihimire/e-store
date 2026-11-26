package com.benkih.estore.product.service;

import com.benkih.estore.product.dto.request.CategoryRequest;
import com.benkih.estore.product.dto.response.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
    CategoryResponseDto addCategory(CategoryRequest request);

    CategoryResponseDto getCategoryBySlug(String slug);

    CategoryResponseDto getCategoryByName(String name);

    List<CategoryResponseDto> getAllCategories();

    List<CategoryResponseDto> getChildCategories(String parentSlug);

    CategoryResponseDto updateCategory(CategoryRequest request, String slug);

    void deleteCategoryBySlug(String slug);
}
