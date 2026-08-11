package com.smartinventorysystem.modules.supplier.specification;

import com.smartinventorysystem.modules.supplier.dto.request.SupplierFilterRequest;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class SupplierSpecification {

    private static final String FIELD_SUPPLIER_NAME = "supplierName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PHONE = "phone";
    private static final String FIELD_ADDRESS = "address";

    private SupplierSpecification() {}

    public static Specification<Supplier> withFilters(SupplierFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addSupplierNamePredicate(predicates, root, cb, filter.getSupplierName());
            addEmailPredicate(predicates, root, cb, filter.getEmail());
            addPhonePredicate(predicates, root, cb, filter.getPhone());
            addAddressPredicate(predicates, root, cb, filter.getAddress());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<Supplier> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        Predicate namePredicate = cb.like(cb.lower(root.get(FIELD_SUPPLIER_NAME)), searchPattern);
        Predicate emailPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_EMAIL), "")), searchPattern);
        Predicate phonePredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_PHONE), "")), searchPattern);
        Predicate addressPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_ADDRESS), "")), searchPattern);

        predicates.add(cb.or(namePredicate, emailPredicate, phonePredicate, addressPredicate));
    }

    private static void addSupplierNamePredicate(List<Predicate> predicates, Root<Supplier> root, CriteriaBuilder cb, String name) {
        if (StringUtils.hasText(name)) {
            String pattern = "%" + name.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(FIELD_SUPPLIER_NAME)), pattern));
        }
    }

    private static void addEmailPredicate(List<Predicate> predicates, Root<Supplier> root, CriteriaBuilder cb, String email) {
        if (StringUtils.hasText(email)) {
            String pattern = "%" + email.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(FIELD_EMAIL), "")), pattern));
        }
    }

    private static void addPhonePredicate(List<Predicate> predicates, Root<Supplier> root, CriteriaBuilder cb, String phone) {
        if (StringUtils.hasText(phone)) {
            String pattern = "%" + phone.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(FIELD_PHONE), "")), pattern));
        }
    }

    private static void addAddressPredicate(List<Predicate> predicates, Root<Supplier> root, CriteriaBuilder cb, String address) {
        if (StringUtils.hasText(address)) {
            String pattern = "%" + address.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(FIELD_ADDRESS), "")), pattern));
        }
    }
}
