package com.smartinventorysystem.modules.product.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.modules.product.dto.request.CreateProductRequest;
import com.smartinventorysystem.modules.product.dto.request.ProductFilterRequest;
import com.smartinventorysystem.modules.product.dto.request.UpdateProductRequest;
import com.smartinventorysystem.modules.product.dto.response.ProductResponse;
import com.smartinventorysystem.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(ApiRoutes.Products.BASE)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Products.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductResponse response = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Product created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PutMapping(ApiRoutes.Products.UPDATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateProductRequest request) {

        ProductResponse response = productService.updateProduct(productId, request);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Products.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Integer productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Products.GET_BY_ID)
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Integer productId) {

        ProductResponse response = productService.getProductById(productId);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Products.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @Valid @ModelAttribute ProductFilterRequest filterRequest) {

        PageResponse<ProductResponse> response = productService.getProducts(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Products fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {

        List<ProductResponse> response = productService.getAllProducts();

        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("All products fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}