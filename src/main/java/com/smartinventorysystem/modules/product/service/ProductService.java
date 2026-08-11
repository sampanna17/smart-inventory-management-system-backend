package com.smartinventorysystem.modules.product.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.product.dto.request.CreateProductRequest;
import com.smartinventorysystem.modules.product.dto.request.ProductFilterRequest;
import com.smartinventorysystem.modules.product.dto.request.UpdateProductRequest;
import com.smartinventorysystem.modules.product.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Integer productId,
                                  UpdateProductRequest request);

    void deleteProduct(Integer productId);

    ProductResponse getProductById(Integer productId);

    List<ProductResponse> getAllProducts();

    PageResponse<ProductResponse> getProducts(ProductFilterRequest request);
}
