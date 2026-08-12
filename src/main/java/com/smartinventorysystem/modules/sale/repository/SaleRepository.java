package com.smartinventorysystem.modules.sale.repository;

import com.smartinventorysystem.enums.SaleStatus;
import com.smartinventorysystem.modules.sale.entity.Sale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Integer>, JpaSpecificationExecutor<Sale> {

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT s FROM Sale s WHERE s.saleID = :id")
    Optional<Sale> findByIdWithCustomer(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT s FROM Sale s")
    List<Sale> findAllWithCustomer();

    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT s FROM Sale s WHERE s.saleID IN :ids")
    List<Sale> findAllByIdInWithCustomer(@Param("ids") List<Integer> ids);

    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT s FROM Sale s WHERE s.customer.customerID = :customerId")
    List<Sale> findByCustomerWithCustomer(@Param("customerId") Integer customerId);

    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT s FROM Sale s WHERE s.status = :status")
    List<Sale> findByStatusWithCustomer(@Param("status") SaleStatus status);

    long countByStatus(SaleStatus status);

    @Query("""
            SELECT COALESCE(SUM(s.totalAmount),0)
            FROM Sale s
            WHERE s.status = :status
            """)
    BigDecimal sumRevenueByStatus(@Param("status") SaleStatus status);

    long countByUserIDAndStatusAndSaleDateBetween(
            Integer userID,
            SaleStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT COALESCE(SUM(s.totalAmount),0)
            FROM Sale s
            WHERE s.userID = :userID
              AND s.status = :status
              AND s.saleDate BETWEEN :start AND :end
            """)
    BigDecimal sumRevenueByUserAndDateBetween(
            @Param("userID") Integer userID,
            @Param("status") SaleStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @EntityGraph(attributePaths = {"customer"})
    List<Sale> findTop5ByUserIDAndStatusOrderBySaleDateDesc(
            Integer userID,
            SaleStatus status
    );
}