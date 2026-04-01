package com.App.lbs_backend.core.specs;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static com.App.lbs_backend.core.utils.AppUtil.isNullString;
import static com.App.lbs_backend.core.utils.AppUtil.pageSize;

public record PaginationCriteria(
        Integer page,
        Integer size,
        String sortField,
        String sortOrder,
        String filter
) {

    public static PaginationCriteria ofByLabel(String order) {
        return new PaginationCriteria(1, Integer.MAX_VALUE, "label", order, "");
    }

    public static PaginationCriteria ofByLabelAsc() {
        return ofByLabel("ASC");
    }

    public static PaginationCriteria ofByLabelDesc() {
        return ofByLabel("DESC");
    }

    public static PaginationCriteria of() {
        return new PaginationCriteria(1, Integer.MAX_VALUE, "id", "DESC", "");
    }

    public static PaginationCriteria all() {
        return of();
    }

    public static PaginationCriteria ofFilter(String filter) {
        return new PaginationCriteria(1, 25, "id", "DESC", filter);
    }

    public Pageable pageable() {
        int pageNumber = (page() == null || page() == 0) ? 0 : page() - 1;
        int pageSizeValue = size() == null ? pageSize() : size();
        Sort.Direction direction = isNullString(sortOrder()) ? Sort.Direction.DESC : Sort.Direction.valueOf(sortOrder().toUpperCase());
        String field = isNullString(sortField()) ? "id" : sortField();
        Sort sort = Sort.by(direction, field);
        return PageRequest.of(pageNumber, pageSizeValue, sort);
    }
}
