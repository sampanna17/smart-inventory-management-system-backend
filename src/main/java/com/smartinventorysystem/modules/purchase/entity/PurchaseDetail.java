package com.smartinventorysystem.modules.purchase.entity;

import com.smartinventorysystem.modules.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Purchase_Details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PurchaseDetailID")
    private Integer purchaseDetailId;

    @ManyToOne
    @JoinColumn(name = "purchaseID")
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(name = "productID")
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}