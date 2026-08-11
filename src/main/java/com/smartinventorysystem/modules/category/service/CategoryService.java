package com.smartinventorysystem.modules.category.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.category.dto.request.CategoryFilterRequest;
import com.smartinventorysystem.modules.category.dto.request.CreateCategoryRequest;
import com.smartinventorysystem.modules.category.dto.request.UpdateCategoryRequest;
import com.smartinventorysystem.modules.category.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Integer id, UpdateCategoryRequest request);

    void deleteCategory(Integer id);

    CategoryResponse getCategoryById(Integer id);

    List<CategoryResponse> getAllCategories();

    PageResponse<CategoryResponse> getCategories(CategoryFilterRequest request);
}