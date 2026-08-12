package com.smartinventorysystem.modules.user.specification;

import com.smartinventorysystem.modules.user.dto.request.UserFilterRequest;
import com.smartinventorysystem.modules.user.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecification {

    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_ROLE = "role";
    private static final String FIELD_STATUS = "status";

    private UserSpecification() {}

    public static Specification<User> withFilters(UserFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addRolePredicate(predicates, root, cb, filter);
            addStatusPredicate(predicates, root, cb, filter);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<User> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        Predicate namePredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_FULL_NAME), "")), searchPattern);
        Predicate emailPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_EMAIL), "")), searchPattern);

        predicates.add(cb.or(namePredicate, emailPredicate));
    }

    private static void addRolePredicate(List<Predicate> predicates, Root<User> root, CriteriaBuilder cb, UserFilterRequest filter) {
        if (filter.getRole() != null) {
            predicates.add(cb.equal(root.get(FIELD_ROLE), filter.getRole()));
        }
    }

    private static void addStatusPredicate(List<Predicate> predicates, Root<User> root, CriteriaBuilder cb, UserFilterRequest filter) {
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get(FIELD_STATUS), filter.getStatus()));
        }
    }
}
