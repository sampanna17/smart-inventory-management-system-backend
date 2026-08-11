package com.smartinventorysystem.modules.supplier.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.supplier.dto.request.CreateSupplierRequest;
import com.smartinventorysystem.modules.supplier.dto.request.SupplierFilterRequest;
import com.smartinventorysystem.modules.supplier.dto.request.UpdateSupplierRequest;
import com.smartinventorysystem.modules.supplier.dto.response.SupplierResponse;
import java.util.List;

public interface SupplierService {
    SupplierResponse createSupplier(CreateSupplierRequest request);
    SupplierResponse updateSupplier(Integer supplierId, UpdateSupplierRequest request);
    void deleteSupplier(Integer supplierId);
    SupplierResponse getSupplierById(Integer supplierId);
    List<SupplierResponse> getAllSuppliers();
    PageResponse<SupplierResponse> getSuppliers(SupplierFilterRequest request);
}