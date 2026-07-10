package com.benkih.estore.product.service;

import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.repository.CategoryRepository;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
  private final CategoryRepository categoryRepository;

  @Override
  public Category addCategory(Category category) {
    return Optional.of(category)
        .filter(data -> !categoryRepository.existsByName(data.getName()))
        .map(categoryRepository :: save)
        .orElseThrow(() -> new AlreadyExistsException(category.getName() + " already exist."));
  }

  @Override
  public Category getCategoryById(String slug) {
    return categoryRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
  }

  @Override
  public Category getCategoryByName(String name) {
    return categoryRepository.findByName(name);
  }

  @Override
  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  @Override
  public Category updateCategory(Category category, String slug) {
    return Optional.ofNullable(getCategoryById(slug)).map(existingCategory -> {
      existingCategory.setName(category.getName());
      return categoryRepository.save(existingCategory);
    }).orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
  }

  @Override
  public void deleteCategoryById(String slug) {
    categoryRepository.findBySlug(slug)
        .ifPresentOrElse(categoryRepository::delete, () -> {
      throw new ResourceNotFoundException("Category not found!");
    });
  }
}
