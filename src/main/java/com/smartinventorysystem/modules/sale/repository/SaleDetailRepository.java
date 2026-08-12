package com.smartinventorysystem.modules.sale.repository;

import com.smartinventorysystem.enums.SaleStatus;
import com.smartinventorysystem.modules.sale.entity.SaleDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, Integer> {

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT sd FROM SaleDetail sd WHERE sd.sale.saleID = :saleId")
    List<SaleDetail> findBySaleIdWithProduct(@Param("saleId") Integer saleId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SaleDetail sd WHERE sd.sale.saleID = :saleId")
    void deleteBySaleId(@Param("saleId") Integer saleId);

    @Query("""
            SELECT COALESCE(SUM(sd.quantity),0)
            FROM SaleDetail sd
            WHERE sd.sale.userID = :userID
              AND sd.sale.status = :status
              AND sd.sale.saleDate BETWEEN :start AND :end
            """)
    long countProductsSoldToday(
            @Param("userID") Integer userID,
            @Param("status") SaleStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
