package com.smartinventorysystem.modules.customer.repository;

import com.smartinventorysystem.modules.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    boolean existsByCustomerName(String customerName);
    boolean existsByEmail(String email);
}