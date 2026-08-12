package com.smartinventorysystem.modules.purchase.repository;

import com.smartinventorysystem.modules.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer>, JpaSpecificationExecutor<Purchase> {

    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);

    @Query("""
    SELECT p
    FROM Purchase p
    LEFT JOIN FETCH p.purchaseDetails pd
    LEFT JOIN FETCH pd.product
    LEFT JOIN FETCH p.supplier
    WHERE p.purchaseId = :id
""")
    Optional<Purchase> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
    SELECT DISTINCT p
    FROM Purchase p
    LEFT JOIN FETCH p.purchaseDetails pd
    LEFT JOIN FETCH pd.product
    LEFT JOIN FETCH p.supplier
""")
    List<Purchase> findAllWithDetails();


    @Query("""
    SELECT DISTINCT p
    FROM Purchase p
    LEFT JOIN FETCH p.purchaseDetails pd
    LEFT JOIN FETCH pd.product
    LEFT JOIN FETCH p.supplier
    WHERE p.supplier.supplierId = :supplierId
""")
    List<Purchase> findBySupplierWithDetails(
            @Param("supplierId") Integer supplierId
    );

    @Query("""
    SELECT DISTINCT p
    FROM Purchase p
    LEFT JOIN FETCH p.purchaseDetails pd
    LEFT JOIN FETCH pd.product
    LEFT JOIN FETCH p.supplier
    WHERE p.purchaseId IN :ids
""")
    List<Purchase> findAllByIdInWithDetails(
            @Param("ids") List<Integer> ids
    );

}