package com.smartinventorysystem.modules.sale.entity;

import com.smartinventorysystem.modules.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Sale_Details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleDetailID")
    private Integer saleDetailID;

    @ManyToOne
    @JoinColumn(name = "saleID")
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "productID")
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
