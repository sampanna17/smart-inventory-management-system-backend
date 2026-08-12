package com.smartinventorysystem.modules.stockmovement.specification;

import com.smartinventorysystem.enums.MovementType;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.stockmovement.dto.request.StockMovementFilterRequest;
import com.smartinventorysystem.modules.stockmovement.entity.StockMovement;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class StockMovementSpecification {

    private static final String FIELD_PRODUCT = "product";
    private static final String FIELD_PRODUCT_ID = "productId";
    private static final String FIELD_PRODUCT_NAME = "productName";
    private static final String FIELD_USER_ID = "userID";
    private static final String FIELD_MOVEMENT_TYPE = "movementType";
    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_MOVEMENT_DATE = "movementDate";
    private static final String FIELD_REMARKS = "remarks";

    private StockMovementSpecification() {}

    public static Specification<StockMovement> withFilters(StockMovementFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addProductIdPredicate(predicates, root, cb, filter.getProductId());
            addUserIdPredicate(predicates, root, cb, filter.getUserId());
            addMovementTypePredicate(predicates, root, cb, filter.getMovementType());
            addDateRangePredicate(predicates, root, cb, filter.getStartDate(), filter.getEndDate());
            addQuantityRangePredicate(predicates, root, cb, filter.getMinQuantity(), filter.getMaxQuantity());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<StockMovement> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";
        Join<StockMovement, Product> productJoin = root.join(FIELD_PRODUCT, JoinType.LEFT);

        Predicate productNamePredicate = cb.like(cb.lower(productJoin.get(FIELD_PRODUCT_NAME)), searchPattern);
        Predicate remarksPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_REMARKS), "")), searchPattern);

        predicates.add(cb.or(productNamePredicate, remarksPredicate));
    }

    private static void addProductIdPredicate(List<Predicate> predicates, Root<StockMovement> root, CriteriaBuilder cb, Integer productId) {
        if (productId != null) {
            Join<StockMovement, Product> productJoin = root.join(FIELD_PRODUCT, JoinType.LEFT);
            predicates.add(cb.equal(productJoin.get(FIELD_PRODUCT_ID), productId));
        }
    }

    private static void addUserIdPredicate(List<Predicate> predicates, Root<StockMovement> root, CriteriaBuilder cb, Integer userId) {
        if (userId != null) {
            predicates.add(cb.equal(root.get(FIELD_USER_ID), userId));
        }
    }

    private static void addMovementTypePredicate(List<Predicate> predicates, Root<StockMovement> root, CriteriaBuilder cb, MovementType movementType) {
        if (movementType != null) {
            predicates.add(cb.equal(root.get(FIELD_MOVEMENT_TYPE), movementType));
        }
    }

    private static void addDateRangePredicate(List<Predicate> predicates, Root<StockMovement> root, CriteriaBuilder cb, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            predicates.add(cb.between(root.get(FIELD_MOVEMENT_DATE), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_MOVEMENT_DATE), startDate));
        } else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_MOVEMENT_DATE), endDate));
        }
    }

    private static void addQuantityRangePredicate(List<Predicate> predicates, Root<StockMovement> root, CriteriaBuilder cb, Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && maxQuantity != null) {
            predicates.add(cb.between(root.get(FIELD_QUANTITY), minQuantity, maxQuantity));
        } else if (minQuantity != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_QUANTITY), minQuantity));
        } else if (maxQuantity != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_QUANTITY), maxQuantity));
        }
    }
}
