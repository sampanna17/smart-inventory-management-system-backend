package com.smartinventorysystem.modules.product.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.exceptions.DuplicateProductException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.category.entity.Category;
import com.smartinventorysystem.modules.category.repository.CategoryRepository;
import com.smartinventorysystem.modules.product.dto.request.CreateProductRequest;
import com.smartinventorysystem.modules.product.dto.request.ProductFilterRequest;
import com.smartinventorysystem.modules.product.dto.request.UpdateProductRequest;
import com.smartinventorysystem.modules.product.dto.response.ProductResponse;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.product.mapper.ProductMapper;
import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.product.specification.ProductSpecification;
import com.smartinventorysystem.modules.unit.entity.Unit;
import com.smartinventorysystem.modules.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsByProductName(request.getProductName())) {
            throw new DuplicateProductException(
                    "Product already exists with name: " + request.getProductName());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

        Product product = productMapper.toEntity(request);

        product.setCategory(category);
        product.setUnit(unit);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.CATEGORY_NOT_FOUND));

            product.setCategory(category);
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.UNIT_NOT_FOUND));

            product.setUnit(unit);
        }

        if (request.getProductName() != null
                && !request.getProductName().isBlank()) {

            if (productRepository.existsByProductNameAndProductIdNot(
                    request.getProductName(), productId)) {

                throw new DuplicateProductException(
                        "Product already exists with name: " + request.getProductName());
            }

            product.setProductName(request.getProductName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }

        if (request.getSellingPrice() != null) {
            product.setSellingPrice(request.getSellingPrice());
        }

        if (request.getReorderLevel() != null) {
            product.setReorderLevel(request.getReorderLevel());
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productMapper.toResponseList(productRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(ProductFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Product> specification = ProductSpecification.withFilters(request);

        Page<Product> productPage = productRepository.findAll(specification, pageable);
        return PageResponse.of(productPage, productMapper::toResponse);
    }

    private Pageable createPageable(ProductFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "createdAt";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "desc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "createdAt";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "name", "productname" -> "productName";
            case "price", "sellingprice" -> "sellingPrice";
            case "costprice" -> "costPrice";
            case "stock", "stockquantity" -> "stockQuantity";
            case "reorderlevel" -> "reorderLevel";
            case "date", "createdat" -> "createdAt";
            case "updatedat" -> "updatedAt";
            case "category", "categoryname" -> "category.categoryName";
            case "unit", "unitname" -> "unit.unitName";
            case "id", "productid" -> "productId";
            default -> "createdAt";
        };
    }
}