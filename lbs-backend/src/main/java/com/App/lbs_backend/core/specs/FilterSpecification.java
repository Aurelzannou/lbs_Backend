package com.App.lbs_backend.core.specs;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

public class FilterSpecification<T> {

    public Specification<T> applyFilters(List<FilterCriteria> filters) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            if (filters == null || filters.isEmpty()) {
                return builder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            for (FilterCriteria criteria : filters) {
                if (criteria.operation().equalsIgnoreCase(">")) {
                    predicates.add(builder.greaterThanOrEqualTo(
                            root.get(criteria.key()), criteria.value().toString()));
                } else if (criteria.operation().equalsIgnoreCase("<")) {
                    predicates.add(builder.lessThanOrEqualTo(
                            root.get(criteria.key()), criteria.value().toString()));
                } else if (criteria.operation().equalsIgnoreCase(":")) {
                    if (root.get(criteria.key()).getJavaType() == String.class) {
                        predicates.add(builder.like(
                                builder.lower(root.get(criteria.key())),
                                "%" + criteria.value().toString().toLowerCase() + "%"));
                    } else {
                        predicates.add(builder.equal(root.get(criteria.key()), criteria.value()));
                    }
                } else if (criteria.operation().equalsIgnoreCase("=")) {
                    predicates.add(builder.equal(root.get(criteria.key()), criteria.value()));
                }
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
