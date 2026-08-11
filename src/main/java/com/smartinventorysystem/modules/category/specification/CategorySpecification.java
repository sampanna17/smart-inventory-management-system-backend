package com.smartinventorysystem.modules.category.specification;

import com.smartinventorysystem.modules.category.dto.request.CategoryFilterRequest;
import com.smartinventorysystem.modules.category.entity.Category;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class CategorySpecification {

    private static final String FIELD_CATEGORY_NAME = "categoryName";
    private static final String FIELD_DESCRIPTION = "description";

    private CategorySpecification() {}

    public static Specification<Category> withFilters(CategoryFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addCategoryNamePredicate(predicates, root, cb, filter.getCategoryName());
            addDescriptionPredicate(predicates, root, cb, filter.getDescription());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<Category> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        Predicate namePredicate = cb.like(cb.lower(root.get(FIELD_CATEGORY_NAME)), searchPattern);
        Predicate descPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_DESCRIPTION), "")), searchPattern);

        predicates.add(cb.or(namePredicate, descPredicate));
    }

    private static void addCategoryNamePredicate(List<Predicate> predicates, Root<Category> root, CriteriaBuilder cb, String name) {
        if (StringUtils.hasText(name)) {
            String pattern = "%" + name.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(FIELD_CATEGORY_NAME)), pattern));
        }
    }

    private static void addDescriptionPredicate(List<Predicate> predicates, Root<Category> root, CriteriaBuilder cb, String description) {
        if (StringUtils.hasText(description)) {
            String pattern = "%" + description.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(FIELD_DESCRIPTION), "")), pattern));
        }
    }
}
