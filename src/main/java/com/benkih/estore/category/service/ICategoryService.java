package com.benkih.estore.category.service;

import com.benkih.estore.category.entity.Category;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.entity.Product;

import java.util.List;


public interface ICategoryService {
  Category addCategory(Category category);
  Category getCategoryById(String slug);
  Category getCategoryByName(String name);
  List<Category> getAllCategories();
  Category updateCategory(Category category, String slug);
  void deleteCategoryById(String slug);

}
