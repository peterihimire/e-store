package com.benkih.estore.product.controller;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.product.dto.request.CategoryRequest;
import com.benkih.estore.product.dto.response.CategoryResponseDto;
import com.benkih.estore.product.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categories", description = "Product categories")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        try {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "success", "Categories returned", categoryService.getAllCategories()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("fail", "Error:", null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> addCategory(
            @Valid @RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "success", categoryService.addCategory(request)));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/category/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryBySlug(
            @PathVariable String slug) {
        try {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "success",
                            "Category returned",
                            categoryService.getCategoryBySlug(slug)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/category/{name}/name")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryByName(
            @PathVariable String name) {
        try {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "success",
                            "Category returned",
                            categoryService.getCategoryByName(name)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @DeleteMapping("/category/{slug}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String slug) {
        try {
            categoryService.deleteCategoryBySlug(slug);
            return ResponseEntity.ok(new ApiResponse<>("success", "Category deleted", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @PutMapping("/category/{slug}/update")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable String slug, @Valid @RequestBody CategoryRequest request) {
        try {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "success",
                            "Update success",
                            categoryService.updateCategory(request, slug)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }
}
