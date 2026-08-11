package com.smartinventorysystem.modules.supplier.repository;

import com.smartinventorysystem.modules.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer>, JpaSpecificationExecutor<Supplier> {
    boolean existsBySupplierName(String supplierName);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // For Update
    boolean existsBySupplierNameAndSupplierIdNot(String supplierName, Integer supplierId);
    boolean existsByEmailAndSupplierIdNot(String email, Integer supplierId);
    boolean existsByPhoneAndSupplierIdNot(String phone, Integer supplierId);
}
