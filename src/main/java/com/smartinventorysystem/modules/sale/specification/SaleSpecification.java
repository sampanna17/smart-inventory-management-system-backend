package com.smartinventorysystem.modules.sale.specification;

import com.smartinventorysystem.enums.SaleStatus;
import com.smartinventorysystem.modules.customer.entity.Customer;
import com.smartinventorysystem.modules.sale.dto.request.SaleFilterRequest;
import com.smartinventorysystem.modules.sale.entity.Sale;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class SaleSpecification {

    private static final String FIELD_INVOICE_NUMBER = "invoiceNumber";
    private static final String FIELD_CUSTOMER = "customer";
    private static final String FIELD_CUSTOMER_NAME = "customerName";
    private static final String FIELD_CUSTOMER_ID = "customerID";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_SALE_DATE = "saleDate";
    private static final String FIELD_TOTAL_AMOUNT = "totalAmount";

    private SaleSpecification() {}

    public static Specification<Sale> withFilters(SaleFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addInvoiceNumberPredicate(predicates, root, cb, filter.getInvoiceNumber());
            addCustomerIdPredicate(predicates, root, cb, filter.getCustomerId());
            addStatusPredicate(predicates, root, cb, filter.getStatus());
            addDateRangePredicate(predicates, root, cb, filter.getStartDate(), filter.getEndDate());
            addAmountRangePredicate(predicates, root, cb, filter.getMinAmount(), filter.getMaxAmount());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<Sale> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";
        Join<Sale, Customer> customerJoin = root.join(FIELD_CUSTOMER, JoinType.LEFT);

        Predicate invoicePredicate = cb.like(cb.lower(root.get(FIELD_INVOICE_NUMBER)), searchPattern);
        Predicate customerNamePredicate = cb.like(cb.lower(customerJoin.get(FIELD_CUSTOMER_NAME)), searchPattern);

        predicates.add(cb.or(invoicePredicate, customerNamePredicate));
    }

    private static void addInvoiceNumberPredicate(List<Predicate> predicates, Root<Sale> root, CriteriaBuilder cb, String invoiceNumber) {
        if (StringUtils.hasText(invoiceNumber)) {
            String pattern = "%" + invoiceNumber.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(FIELD_INVOICE_NUMBER)), pattern));
        }
    }

    private static void addCustomerIdPredicate(List<Predicate> predicates, Root<Sale> root, CriteriaBuilder cb, Integer customerId) {
        if (customerId != null) {
            Join<Sale, Customer> customerJoin = root.join(FIELD_CUSTOMER, JoinType.LEFT);
            predicates.add(cb.equal(customerJoin.get(FIELD_CUSTOMER_ID), customerId));
        }
    }

    private static void addStatusPredicate(List<Predicate> predicates, Root<Sale> root, CriteriaBuilder cb, SaleStatus status) {
        if (status != null) {
            predicates.add(cb.equal(root.get(FIELD_STATUS), status));
        }
    }

    private static void addDateRangePredicate(List<Predicate> predicates, Root<Sale> root, CriteriaBuilder cb, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            predicates.add(cb.between(root.get(FIELD_SALE_DATE), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_SALE_DATE), startDate));
        } else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_SALE_DATE), endDate));
        }
    }

    private static void addAmountRangePredicate(List<Predicate> predicates, Root<Sale> root, CriteriaBuilder cb, BigDecimal minAmount, BigDecimal maxAmount) {
        if (minAmount != null && maxAmount != null) {
            predicates.add(cb.between(root.get(FIELD_TOTAL_AMOUNT), minAmount, maxAmount));
        } else if (minAmount != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_TOTAL_AMOUNT), minAmount));
        } else if (maxAmount != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_TOTAL_AMOUNT), maxAmount));
        }
    }
}
