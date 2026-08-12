package com.smartinventorysystem.modules.customer.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.exceptions.BadRequestException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.customer.dto.request.CreateCustomerRequest;
import com.smartinventorysystem.modules.customer.dto.request.CustomerFilterRequest;
import com.smartinventorysystem.modules.customer.dto.request.UpdateCustomerRequest;
import com.smartinventorysystem.modules.customer.dto.response.CustomerResponse;
import com.smartinventorysystem.modules.customer.entity.Customer;
import com.smartinventorysystem.modules.customer.mapper.CustomerMapper;
import com.smartinventorysystem.modules.customer.repository.CustomerRepository;
import com.smartinventorysystem.modules.customer.specification.CustomerSpecification;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final Clock clock;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Customer already exists with email: " + request.getEmail());
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setCreatedAt(LocalDateTime.now(clock));

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Integer customerId, UpdateCustomerRequest request) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.CUSTOMER_NOT_FOUND));

        if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            customer.setCustomerName(request.getCustomerName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customer.setPhone(request.getPhone());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            customer.setEmail(request.getEmail());
        }

        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }

        customer.setUpdatedAt(LocalDateTime.now(clock));

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void deleteCustomer(Integer customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.CUSTOMER_NOT_FOUND));

        customerRepository.delete(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Integer customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.CUSTOMER_NOT_FOUND));

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerMapper.toResponseList(customerRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getCustomers(CustomerFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Customer> specification = CustomerSpecification.withFilters(request);

        Page<Customer> customerPage = customerRepository.findAll(specification, pageable);
        return PageResponse.of(customerPage, customerMapper::toResponse);
    }

    private Pageable createPageable(CustomerFilterRequest request) {
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
            case "name", "customername" -> "customerName";
            case "email" -> "email";
            case "phone" -> "phone";
            case "address" -> "address";
            case "date", "createdat" -> "createdAt";
            case "updatedat" -> "updatedAt";
            case "id", "customerid" -> "customerID";
            default -> "createdAt";
        };
    }
}