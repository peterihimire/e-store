package com.benkih.estore.product.service;

import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.repository.CategoryRepository;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import com.benkih.estore.common.exceptions.BadRequestException;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService implements ICategoryService {
  private final CategoryRepository categoryRepository;


  @Override
  public Category addCategory(Category category) {

    if (category == null) {
      throw new BadRequestException("Category cannot be null.");
    }

    if (category.getName() == null ||
        category.getName().isBlank()) {

      throw new BadRequestException(
          "Category name is required."
      );
    }

    String name = category.getName().trim();

    Category parent = category.getParent();

    boolean exists;

    if (parent == null) {
      exists = categoryRepository.existsByNameAndParentIsNull(name);
    } else {
      exists = categoryRepository.existsByNameAndParent(name, parent);
    }

    if (exists) {
      throw new AlreadyExistsException(name + " already exists.");
    }

    category.setName(name);

    return categoryRepository.save(category);
  }


  @Override
  @Transactional(readOnly = true)
  public Category getCategoryBySlug(String slug) {

    return categoryRepository.findBySlug(slug)
        .orElseThrow(() ->
            new ResourceNotFoundException("Category not found!")
        );
  }


  @Override
  @Transactional(readOnly = true)
  public Category getCategoryByName(String name) {

    if (name == null || name.isBlank()) {
      throw new BadRequestException(
          "Category name is required."
      );
    }

    return categoryRepository.findByNameAndParentIsNull(name.trim())
        .orElseThrow(() ->
            new ResourceNotFoundException("Category not found!")
        );
  }


  @Override
  @Transactional(readOnly = true)
  public List<Category> getAllCategories() {

    return categoryRepository.findByParentIsNull();
  }


  @Transactional(readOnly = true)
  public List<Category> getChildCategories(String parentSlug) {

    Category parent = getCategoryBySlug(parentSlug);

    return categoryRepository.findByParent(parent);
  }


  @Override
  public Category updateCategory(
      Category category,
      String slug
  ) {

    if (category == null) {
      throw new BadRequestException("Category cannot be null.");
    }

    if (category.getName() == null || category.getName().isBlank()) {

      throw new BadRequestException("Category name is required.");
    }

    Category existingCategory = getCategoryBySlug(slug);

    String newName = category.getName().trim();

    Category parent = existingCategory.getParent();

    boolean nameChanged = !existingCategory.getName().equalsIgnoreCase(newName);

    if (nameChanged) {
      boolean exists;

      if (parent == null) {
        exists = categoryRepository.existsByNameAndParentIsNull(newName);
      } else {
        exists = categoryRepository.existsByNameAndParent(
                    newName,
                    parent
                );
      }

      if (exists) {
        throw new AlreadyExistsException(newName + " already exists.");
      }
    }

    existingCategory.setName(newName);

    return categoryRepository.save(existingCategory);
  }


  @Override
  public void deleteCategoryBySlug(String slug) {

    Category category = categoryRepository.findBySlug(slug)
            .orElseThrow(() ->
                new ResourceNotFoundException("Category not found!")
            );

    category.setActive(false);

    categoryRepository.save(category);
  }
}
//@Service
//@RequiredArgsConstructor
//public class CategoryService implements ICategoryService {
//  private final CategoryRepository categoryRepository;
//
//
//  @Override
//  public Category addCategory(Category category) {
//    return Optional.of(category)
//        .filter(data -> !categoryRepository.existsByName(data.getName()))
//        .map(categoryRepository :: save)
//        .orElseThrow(() -> new AlreadyExistsException(category.getName() + " already exist."));
//  }
//
//
//  @Override
//  public Category getCategoryById(String slug) {
//    return categoryRepository.findBySlug(slug)
//        .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
//  }
//
//
//  @Override
//  public Category getCategoryByName(String name, Long businessId) {
//    return categoryRepository.findByName(name);
//  }
//
//
//  @Override
//  public List<Category> getAllCategories() {
//    return categoryRepository.findAll();
//  }
//
//
//  @Override
//  public Category updateCategory(Category category, String slug) {
//    return Optional.ofNullable(getCategoryById(slug)).map(existingCategory -> {
//      existingCategory.setName(category.getName());
//      return categoryRepository.save(existingCategory);
//    }).orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
//  }
//
//
//  @Override
//  public void deleteCategoryById(String slug) {
//    categoryRepository.findBySlug(slug)
//        .ifPresentOrElse(categoryRepository::delete, () -> {
//      throw new ResourceNotFoundException("Category not found!");
//    });
//  }
//}
