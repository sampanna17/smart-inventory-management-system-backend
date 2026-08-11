package com.smartinventorysystem.modules.productsupplier.service;

import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.productsupplier.dto.response.ProductSummaryResponse;

import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.productsupplier.dto.response.SupplierSummaryResponse;
import com.smartinventorysystem.modules.productsupplier.entity.ProductSupplier;
import com.smartinventorysystem.modules.productsupplier.entity.ProductSupplierId;
import com.smartinventorysystem.modules.productsupplier.mapper.ProductSupplierMapper;
import com.smartinventorysystem.modules.productsupplier.repository.ProductSupplierRepository;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
import com.smartinventorysystem.modules.supplier.repository.SupplierRepository;
import com.smartinventorysystem.exceptions.BadRequestException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductSupplierServiceImpl implements ProductSupplierService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final ProductSupplierMapper mapper;

    @Override
    public void addSupplier(Integer productId, Integer supplierId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found")
                );

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Supplier not found")
                );

        if (productSupplierRepository.existsByProductProductIdAndSupplierSupplierId(
                productId,
                supplierId
        )) {

            throw new BadRequestException(
                    "Supplier is already assigned to this product"
            );
        }

        ProductSupplierId id = new ProductSupplierId();
        id.setProductID(productId);
        id.setSupplierID(supplierId);

        ProductSupplier productSupplier = new ProductSupplier();
        productSupplier.setId(id);
        productSupplier.setProduct(product);
        productSupplier.setSupplier(supplier);

        productSupplierRepository.save(productSupplier);
    }

    @Override
    public void removeSupplier(Integer productId, Integer supplierId) {

        if (!productSupplierRepository.existsByProductProductIdAndSupplierSupplierId(
                productId,
                supplierId
        )) {

            throw new ResourceNotFoundException(
                    "Product-Supplier relationship not found"
            );
        }

        ProductSupplierId id = new ProductSupplierId();
        id.setProductID(productId);
        id.setSupplierID(supplierId);

        productSupplierRepository.deleteById(id);
    }

    @Override
    public List<SupplierSummaryResponse> getSuppliersByProduct(Integer productId) {

        if (!productRepository.existsById(productId)) {

            throw new ResourceNotFoundException(
                    "Product not found"
            );
        }

        return mapper.toSupplierResponseList(
                productSupplierRepository.findByProductProductId(productId)
        );
    }

    @Override
    public List<ProductSummaryResponse> getProductsBySupplier(Integer supplierId) {

        if (!supplierRepository.existsById(supplierId)) {

            throw new ResourceNotFoundException(
                    "Supplier not found"
            );
        }

        return mapper.toProductResponseList(
                productSupplierRepository.findBySupplierSupplierId(supplierId)
        );
    }
}