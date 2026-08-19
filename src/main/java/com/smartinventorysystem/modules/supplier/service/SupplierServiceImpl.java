package com.smartinventorysystem.modules.supplier.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.exceptions.DuplicateResourceException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.supplier.dto.request.CreateSupplierRequest;
import com.smartinventorysystem.modules.supplier.dto.request.SupplierFilterRequest;
import com.smartinventorysystem.modules.supplier.dto.request.UpdateSupplierRequest;
import com.smartinventorysystem.modules.supplier.dto.response.SupplierResponse;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
import com.smartinventorysystem.modules.supplier.mapper.SupplierMapper;
import com.smartinventorysystem.modules.supplier.repository.SupplierRepository;
import com.smartinventorysystem.modules.supplier.specification.SupplierSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final Clock clock;

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {

        if (supplierRepository.existsBySupplierName(request.getSupplierName())) {
            throw new DuplicateResourceException("Supplier already exists with name: " + request.getSupplierName());
        }

        if (request.getEmail() != null && supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Supplier already exists with email: " + request.getEmail());
        }

        if (request.getPhone() != null && supplierRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Supplier already exists with Phone: " + request.getPhone());
        }

        Supplier supplier = supplierMapper.toEntity(request);
        supplier.setCreatedAt(LocalDateTime.now(clock));

        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Integer supplierId, UpdateSupplierRequest request) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.SUPPLIER_NOT_FOUND));

        if (request.getSupplierName() != null
                && !request.getSupplierName().isBlank()) {

            if (supplierRepository.existsBySupplierNameAndSupplierIdNot(
                    request.getSupplierName(), supplierId)) {

                throw new DuplicateResourceException(
                        "Supplier already exists with name: " + request.getSupplierName()
                );
            }

            supplier.setSupplierName(request.getSupplierName());
        }

        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {

            if (supplierRepository.existsByEmailAndSupplierIdNot(
                    request.getEmail(), supplierId)) {

                throw new DuplicateResourceException(
                        "Supplier already exists with email: " + request.getEmail()
                );
            }

            supplier.setEmail(request.getEmail());
        }

        if (request.getPhone() != null
                && !request.getPhone().isBlank()) {

            if (supplierRepository.existsByPhoneAndSupplierIdNot(
                    request.getPhone(), supplierId)) {

                throw new DuplicateResourceException(
                        "Supplier already exists with phone: " + request.getPhone()
                );
            }

            supplier.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }

        supplier.setUpdatedAt(LocalDateTime.now(clock));

        return supplierMapper.toResponse(
                supplierRepository.save(supplier)
        );
    }

    @Override
    @Transactional
    public void deleteSupplier(Integer supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.SUPPLIER_NOT_FOUND));

        supplierRepository.delete(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Integer supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.SUPPLIER_NOT_FOUND));

        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierMapper.toResponseList(supplierRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> getSuppliers(SupplierFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Supplier> specification = SupplierSpecification.withFilters(request);

        Page<Supplier> supplierPage = supplierRepository.findAll(specification, pageable);
        return PageResponse.of(supplierPage, supplierMapper::toResponse);
    }

    private Pageable createPageable(SupplierFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "createdAt";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "desc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "createdAt";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "name", "suppliername" -> "supplierName";
            case "email" -> "email";
            case "phone" -> "phone";
            case "address" -> "address";
            case "date", "createdat" -> "createdAt";
            case "updatedat" -> "updatedAt";
            case "id", "supplierid" -> "supplierId";
            default -> "createdAt";
        };
    }
}