package com.smartinventorysystem.modules.unit.specification;

import com.smartinventorysystem.modules.unit.dto.request.UnitFilterRequest;
import com.smartinventorysystem.modules.unit.entity.Unit;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class UnitSpecification {

    private static final String FIELD_UNIT_NAME = "unitName";

    private UnitSpecification() {}

    public static Specification<Unit> withFilters(UnitFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addUnitNamePredicate(predicates, root, cb, filter.getUnitName());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<Unit> root, CriteriaBuilder cb, String search) {
        if (StringUtils.hasText(search)) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(FIELD_UNIT_NAME)), searchPattern));
        }
    }

    private static void addUnitNamePredicate(List<Predicate> predicates, Root<Unit> root, CriteriaBuilder cb, String unitName) {
        if (StringUtils.hasText(unitName)) {
            String pattern = "%" + unitName.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(FIELD_UNIT_NAME)), pattern));
        }
    }
}
