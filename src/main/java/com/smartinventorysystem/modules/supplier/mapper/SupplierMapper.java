package com.smartinventorysystem.modules.supplier.mapper;

import com.smartinventorysystem.modules.supplier.dto.request.CreateSupplierRequest;
import com.smartinventorysystem.modules.supplier.dto.response.SupplierResponse;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "supplierId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Supplier toEntity(CreateSupplierRequest request);

    @Mapping(source = "supplierId", target = "supplierID")
    SupplierResponse toResponse(Supplier supplier);

    List<SupplierResponse> toResponseList(List<Supplier> suppliers);
}