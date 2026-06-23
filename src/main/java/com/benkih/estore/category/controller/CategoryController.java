package com.benkih.estore.category.controller;


import com.benkih.estore.category.entity.Category;
import com.benkih.estore.category.service.ICategoryService;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
  private final ICategoryService categoryService;

  @GetMapping("/all")
  public ResponseEntity<ApiResponse> getAllCategories(){
    try {
      List<Category> categories = categoryService.getAllCategories();
      return ResponseEntity.ok(new ApiResponse("Categories returned", categories));
    } catch (Exception e) {
      return  ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Error:", INTERNAL_SERVER_ERROR));
    }
  }

  @PostMapping("/add")
  public ResponseEntity<ApiResponse> addCategory(@RequestBody Category name){
    try {
      Category category = categoryService.addCategory(name);
      return ResponseEntity.ok(new ApiResponse("success", category));
    } catch (AlreadyExistsException e) {
      return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(),null));
    }
  }

  @GetMapping("/category/{id}/id")
  public ResponseEntity<ApiResponse> getCategoryById(@PathVariable String id){
    try {
      Category category = categoryService.getCategoryById(id);
      return ResponseEntity.ok(new ApiResponse("Category returned", category));
    } catch (ResourceNotFoundException e) {
     return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @GetMapping("/category/{name}/name")
  public ResponseEntity<ApiResponse> getCategoryByName(@PathVariable String name){
    try {
      Category category = categoryService.getCategoryByName(name);
      return ResponseEntity.ok(new ApiResponse("Category returned", category));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @DeleteMapping("/category/{slug}/delete")
  public ResponseEntity<ApiResponse> deleteCategory(@PathVariable String slug){
    try {
      categoryService.deleteCategoryById(slug);
      return ResponseEntity.ok(new ApiResponse("Image deleted", null));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @PutMapping("/category/{slug}/update")
  public ResponseEntity<ApiResponse> updateCategory(@PathVariable String slug, @RequestBody Category category){
    try {
     Category data = categoryService.updateCategory(category, slug);
      return ResponseEntity.ok(new ApiResponse("Update success", data));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

}
