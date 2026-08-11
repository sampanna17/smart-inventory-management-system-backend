package com.smartinventorysystem.modules.product.specification;

import com.smartinventorysystem.modules.category.entity.Category;
import com.smartinventorysystem.modules.product.dto.request.ProductFilterRequest;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.unit.entity.Unit;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private static final String FIELD_STOCK_QUANTITY = "stockQuantity";
    private static final String FIELD_REORDER_LEVEL = "reorderLevel";
    private static final String FIELD_SELLING_PRICE = "sellingPrice";
    private static final String FIELD_PRODUCT_NAME = "productName";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_CATEGORY_ID = "categoryId";
    private static final String FIELD_CATEGORY_NAME = "categoryName";
    private static final String FIELD_UNIT = "unit";
    private static final String FIELD_UNIT_ID = "unitId";
    private static final String FIELD_UNIT_NAME = "unitName";

    private ProductSpecification() {}

    public static Specification<Product> withFilters(ProductFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addCategoryPredicate(predicates, root, cb, filter.getCategoryId());
            addUnitPredicate(predicates, root, cb, filter.getUnitId());
            addStockStatusPredicate(predicates, root, cb, filter.getStockStatus());
            addPriceRangePredicate(predicates, root, cb, filter.getMinPrice(), filter.getMaxPrice());
            addStockRangePredicate(predicates, root, cb, filter.getMinStock(), filter.getMaxStock());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<Product> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        Predicate namePredicate = cb.like(cb.lower(root.get(FIELD_PRODUCT_NAME)), searchPattern);
        Predicate descPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_DESCRIPTION), "")), searchPattern);

        Join<Product, Category> categoryJoin = root.join(FIELD_CATEGORY, JoinType.LEFT);
        Predicate categoryPredicate = cb.like(cb.lower(categoryJoin.get(FIELD_CATEGORY_NAME)), searchPattern);

        Join<Product, Unit> unitJoin = root.join(FIELD_UNIT, JoinType.LEFT);
        Predicate unitPredicate = cb.like(cb.lower(unitJoin.get(FIELD_UNIT_NAME)), searchPattern);

        predicates.add(cb.or(namePredicate, descPredicate, categoryPredicate, unitPredicate));
    }

    private static void addCategoryPredicate(List<Predicate> predicates, Root<Product> root, CriteriaBuilder cb, Integer categoryId) {
        if (categoryId != null) {
            predicates.add(cb.equal(root.get(FIELD_CATEGORY).get(FIELD_CATEGORY_ID), categoryId));
        }
    }

    private static void addUnitPredicate(List<Predicate> predicates, Root<Product> root, CriteriaBuilder cb, Integer unitId) {
        if (unitId != null) {
            predicates.add(cb.equal(root.get(FIELD_UNIT).get(FIELD_UNIT_ID), unitId));
        }
    }

    private static void addStockStatusPredicate(List<Predicate> predicates, Root<Product> root, CriteriaBuilder cb, String stockStatus) {
        if (!StringUtils.hasText(stockStatus)) {
            return;
        }

        String status = stockStatus.trim().toLowerCase();
        switch (status) {
            case "instock", "in_stock" ->
                    predicates.add(cb.greaterThan(root.get(FIELD_STOCK_QUANTITY), root.get(FIELD_REORDER_LEVEL)));
            case "lowstock", "low_stock" -> {
                predicates.add(cb.greaterThan(root.get(FIELD_STOCK_QUANTITY), 0));
                predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_STOCK_QUANTITY), root.get(FIELD_REORDER_LEVEL)));
            }
            case "outofstock", "out_of_stock" ->
                    predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_STOCK_QUANTITY), 0));
            default -> {
                // "all" or unrecognized - no predicate added
            }
        }
    }

    private static void addPriceRangePredicate(List<Predicate> predicates, Root<Product> root, CriteriaBuilder cb, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_SELLING_PRICE), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_SELLING_PRICE), maxPrice));
        }
    }

    private static void addStockRangePredicate(List<Predicate> predicates, Root<Product> root, CriteriaBuilder cb, Integer minStock, Integer maxStock) {
        if (minStock != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_STOCK_QUANTITY), minStock));
        }
        if (maxStock != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_STOCK_QUANTITY), maxStock));
        }
    }
}
