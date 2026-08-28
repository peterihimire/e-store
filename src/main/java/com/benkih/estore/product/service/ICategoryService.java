package com.benkih.estore.product.service;

import com.benkih.estore.product.entity.Category;

import java.util.List;



public interface ICategoryService {

  Category addCategory(Category category);

  Category getCategoryBySlug(String slug);

  Category getCategoryByName(String name);

  List<Category> getAllCategories();

  List<Category> getChildCategories(String parentSlug);

  Category updateCategory(
      Category category,
      String slug
  );

  void deleteCategoryBySlug(String slug);
}
//public interface ICategoryService {
//  Category addCategory(Category category);
//  Category getCategoryById(String slug);
//  Category getCategoryByName(String name, Long businessId);
//  List<Category> getAllCategories();
//  Category updateCategory(Category category, String slug);
//  void deleteCategoryById(String slug);
//
//}
