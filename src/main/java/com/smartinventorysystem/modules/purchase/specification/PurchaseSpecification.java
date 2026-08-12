package com.smartinventorysystem.modules.purchase.specification;

import com.smartinventorysystem.enums.PurchaseStatus;
import com.smartinventorysystem.modules.purchase.dto.request.PurchaseFilterRequest;
import com.smartinventorysystem.modules.purchase.entity.Purchase;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
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

public final class PurchaseSpecification {

    private static final String FIELD_PURCHASE_NUMBER = "purchaseNumber";
    private static final String FIELD_SUPPLIER = "supplier";
    private static final String FIELD_SUPPLIER_NAME = "supplierName";
    private static final String FIELD_SUPPLIER_ID = "supplierId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_PURCHASE_DATE = "purchaseDate";
    private static final String FIELD_TOTAL_AMOUNT = "totalAmount";

    private PurchaseSpecification() {}

    public static Specification<Purchase> withFilters(PurchaseFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addPurchaseNumberPredicate(predicates, root, cb, filter.getPurchaseNumber());
            addSupplierIdPredicate(predicates, root, cb, filter.getSupplierId());
            addStatusPredicate(predicates, root, cb, filter.getStatus());
            addDateRangePredicate(predicates, root, cb, filter.getStartDate(), filter.getEndDate());
            addAmountRangePredicate(predicates, root, cb, filter.getMinAmount(), filter.getMaxAmount());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<Purchase> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";
        Join<Purchase, Supplier> supplierJoin = root.join(FIELD_SUPPLIER, JoinType.LEFT);

        Predicate poNumberPredicate = cb.like(cb.lower(root.get(FIELD_PURCHASE_NUMBER)), searchPattern);
        Predicate supplierNamePredicate = cb.like(cb.lower(supplierJoin.get(FIELD_SUPPLIER_NAME)), searchPattern);

        predicates.add(cb.or(poNumberPredicate, supplierNamePredicate));
    }

    private static void addPurchaseNumberPredicate(List<Predicate> predicates, Root<Purchase> root, CriteriaBuilder cb, String purchaseNumber) {
        if (StringUtils.hasText(purchaseNumber)) {
            String pattern = "%" + purchaseNumber.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(FIELD_PURCHASE_NUMBER)), pattern));
        }
    }

    private static void addSupplierIdPredicate(List<Predicate> predicates, Root<Purchase> root, CriteriaBuilder cb, Integer supplierId) {
        if (supplierId != null) {
            Join<Purchase, Supplier> supplierJoin = root.join(FIELD_SUPPLIER, JoinType.LEFT);
            predicates.add(cb.equal(supplierJoin.get(FIELD_SUPPLIER_ID), supplierId));
        }
    }

    private static void addStatusPredicate(List<Predicate> predicates, Root<Purchase> root, CriteriaBuilder cb, PurchaseStatus status) {
        if (status != null) {
            predicates.add(cb.equal(root.get(FIELD_STATUS), status));
        }
    }

    private static void addDateRangePredicate(List<Predicate> predicates, Root<Purchase> root, CriteriaBuilder cb, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            predicates.add(cb.between(root.get(FIELD_PURCHASE_DATE), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_PURCHASE_DATE), startDate));
        } else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_PURCHASE_DATE), endDate));
        }
    }

    private static void addAmountRangePredicate(List<Predicate> predicates, Root<Purchase> root, CriteriaBuilder cb, BigDecimal minAmount, BigDecimal maxAmount) {
        if (minAmount != null && maxAmount != null) {
            predicates.add(cb.between(root.get(FIELD_TOTAL_AMOUNT), minAmount, maxAmount));
        } else if (minAmount != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_TOTAL_AMOUNT), minAmount));
        } else if (maxAmount != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_TOTAL_AMOUNT), maxAmount));
        }
    }
}
