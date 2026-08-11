package com.smartinventorysystem.modules.product.repository;

import com.smartinventorysystem.modules.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    boolean existsByProductName(String productName);
    boolean existsByProductNameAndProductIdNot(String productName, Integer productId);

    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.stockQuantity > 0
              AND p.stockQuantity <= p.reorderLevel
            """)
    long countLowStockProducts();

    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.stockQuantity <= 0
            """)
    long countOutOfStockProducts();

}