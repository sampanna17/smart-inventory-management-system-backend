package com.smartinventorysystem.modules.stockmovement.mapper;

import com.smartinventorysystem.modules.stockmovement.dto.request.CreateStockMovementRequest;
import com.smartinventorysystem.modules.stockmovement.dto.response.StockMovementResponse;
import com.smartinventorysystem.modules.stockmovement.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(target = "movementID", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "userID", ignore = true)
    @Mapping(target = "movementDate", ignore = true)
    StockMovement toEntity(CreateStockMovementRequest request);

    @Mapping(source = "movementID", target = "movementId")
    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "userID", target = "userId")
    @Mapping(target = "userName", ignore = true)
    StockMovementResponse toResponse(StockMovement stockMovement);

    List<StockMovementResponse> toResponseList(List<StockMovement> stockMovements);
}