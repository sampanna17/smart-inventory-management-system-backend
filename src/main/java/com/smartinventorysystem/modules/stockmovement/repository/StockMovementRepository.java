package com.smartinventorysystem.modules.stockmovement.repository;

import com.smartinventorysystem.enums.MovementType;
import com.smartinventorysystem.modules.stockmovement.entity.StockMovement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Integer>, JpaSpecificationExecutor<StockMovement> {

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementID = :id")
    Optional<StockMovement> findByIdWithProduct(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT sm FROM StockMovement sm")
    List<StockMovement> findAllWithProduct();

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.productId = :productId")
    List<StockMovement> findByProductWithProduct(@Param("productId") Integer productId);

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT sm FROM StockMovement sm WHERE sm.userID = :userId")
    List<StockMovement> findByUserWithProduct(@Param("userId") Integer userId);

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementType = :movementType")
    List<StockMovement> findByMovementTypeWithProduct(@Param("movementType") MovementType movementType);

}
