package com.smartinventorysystem.modules.product.entity;

import com.smartinventorysystem.modules.category.entity.Category;
import com.smartinventorysystem.modules.unit.entity.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Column(name = "productID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "categoryID")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "unitID")
    private Unit unit;

    private String productName;
    private String description;

    private BigDecimal costPrice;
    private BigDecimal sellingPrice;

    private Integer stockQuantity;
    private Integer reorderLevel;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}