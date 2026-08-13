package com.smartinventorysystem.modules.product.mapper;

import com.smartinventorysystem.modules.product.dto.request.CreateProductRequest;
import com.smartinventorysystem.modules.product.dto.response.ProductResponse;
import com.smartinventorysystem.modules.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "unit.unitId", target = "unitId")
    @Mapping(source = "unit.unitName", target = "unitName")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);
}