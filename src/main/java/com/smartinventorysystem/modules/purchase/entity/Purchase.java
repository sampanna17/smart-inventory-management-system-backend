package com.smartinventorysystem.modules.purchase.entity;

import com.smartinventorysystem.enums.PurchaseStatus;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Purchases")
@Data
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PurchaseID")
    private Integer purchaseId;

    @Column(name = "PurchaseNumber", nullable = false, unique = true, length = 30)
    private String purchaseNumber;

    @ManyToOne
    @JoinColumn(name = "supplierID")
    private Supplier supplier;

    private Integer userID;

    private LocalDateTime purchaseDate;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;

    @CreatedDate
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "purchase",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseDetail> purchaseDetails = new ArrayList<>();
}
