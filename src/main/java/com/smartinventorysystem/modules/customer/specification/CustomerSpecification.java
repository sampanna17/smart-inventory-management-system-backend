package com.smartinventorysystem.modules.customer.specification;

import com.smartinventorysystem.modules.customer.dto.request.CustomerFilterRequest;
import com.smartinventorysystem.modules.customer.entity.Customer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class CustomerSpecification {

    private static final String FIELD_CUSTOMER_NAME = "customerName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PHONE = "phone";
    private static final String FIELD_ADDRESS = "address";

    private CustomerSpecification() {}

    public static Specification<Customer> withFilters(CustomerFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addSearchPredicate(predicates, root, cb, filter.getSearch());
            addCustomerNamePredicate(predicates, root, cb, filter.getCustomerName());
            addEmailPredicate(predicates, root, cb, filter.getEmail());
            addPhonePredicate(predicates, root, cb, filter.getPhone());
            addAddressPredicate(predicates, root, cb, filter.getAddress());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSearchPredicate(List<Predicate> predicates, Root<Customer> root, CriteriaBuilder cb, String search) {
        if (!StringUtils.hasText(search)) {
            return;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        Predicate namePredicate = cb.like(cb.lower(root.get(FIELD_CUSTOMER_NAME)), searchPattern);
        Predicate emailPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_EMAIL), "")), searchPattern);
        Predicate phonePredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_PHONE), "")), searchPattern);
        Predicate addressPredicate = cb.like(cb.lower(cb.coalesce(root.get(FIELD_ADDRESS), "")), searchPattern);

        predicates.add(cb.or(namePredicate, emailPredicate, phonePredicate, addressPredicate));
    }

    private static void addCustomerNamePredicate(List<Predicate> predicates, Root<Customer> root, CriteriaBuilder cb, String customerName) {
        if (StringUtils.hasText(customerName)) {
            String pattern = "%" + customerName.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get(FIELD_CUSTOMER_NAME)), pattern));
        }
    }

    private static void addEmailPredicate(List<Predicate> predicates, Root<Customer> root, CriteriaBuilder cb, String email) {
        if (StringUtils.hasText(email)) {
            String pattern = "%" + email.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(FIELD_EMAIL), "")), pattern));
        }
    }

    private static void addPhonePredicate(List<Predicate> predicates, Root<Customer> root, CriteriaBuilder cb, String phone) {
        if (StringUtils.hasText(phone)) {
            String pattern = "%" + phone.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(FIELD_PHONE), "")), pattern));
        }
    }

    private static void addAddressPredicate(List<Predicate> predicates, Root<Customer> root, CriteriaBuilder cb, String address) {
        if (StringUtils.hasText(address)) {
            String pattern = "%" + address.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(FIELD_ADDRESS), "")), pattern));
        }
    }
}
