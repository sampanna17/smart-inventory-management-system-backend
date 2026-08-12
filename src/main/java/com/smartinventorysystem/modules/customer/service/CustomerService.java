package com.smartinventorysystem.modules.customer.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.customer.dto.request.CreateCustomerRequest;
import com.smartinventorysystem.modules.customer.dto.request.CustomerFilterRequest;
import com.smartinventorysystem.modules.customer.dto.request.UpdateCustomerRequest;
import com.smartinventorysystem.modules.customer.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse updateCustomer(Integer customerId, UpdateCustomerRequest request);

    void deleteCustomer(Integer customerId);

    CustomerResponse getCustomerById(Integer customerId);

    List<CustomerResponse> getAllCustomers();

    PageResponse<CustomerResponse> getCustomers(CustomerFilterRequest request);
}