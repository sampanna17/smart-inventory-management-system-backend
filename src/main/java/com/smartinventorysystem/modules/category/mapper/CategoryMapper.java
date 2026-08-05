package com.smartinventorysystem.modules.category.mapper;

import com.smartinventorysystem.modules.category.dto.request.CreateCategoryRequest;
import com.smartinventorysystem.modules.category.dto.response.CategoryResponse;
import com.smartinventorysystem.modules.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(source = "categoryId", target = "categoryID")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);
}