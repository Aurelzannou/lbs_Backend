package com.App.lbs_backend.core.specs;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PaginationCriteria(
        Integer page,
        Integer size,
        String sortField,
        String sortOrder,
        String filter
) {

    public static PaginationCriteria of() {
        return new PaginationCriteria(1, 25, "id", "DESC", "");
    }

    public Pageable pageable() {
        int targetPage = (page() == null || page() == 0) ? 0 : page() - 1;
        int targetSize = (size() == null || size() == 0) ? 25 : size();
        
        Sort.Direction direction = (sortOrder() == null || sortOrder().trim().isEmpty() || "DESC".equalsIgnoreCase(sortOrder())) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
                
        String field = (sortField() == null || sortField().trim().isEmpty()) ? "id" : sortField();
        
        Sort sort = Sort.by(direction, field);
        return PageRequest.of(targetPage, targetSize, sort);
    }
}
