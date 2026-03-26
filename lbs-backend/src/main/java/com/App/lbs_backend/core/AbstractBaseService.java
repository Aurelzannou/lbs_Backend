package com.App.lbs_backend.core;

import com.App.lbs_backend.core.exception.EntityNotFoundException;
import com.App.lbs_backend.core.http.request.IdsRequest;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.MetaResponse;
import com.App.lbs_backend.core.http.response.PageResponse;
import com.App.lbs_backend.core.specs.FilterCriteria;
import com.App.lbs_backend.core.specs.FilterSpecification;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.core.utils.AppUtil;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractBaseService<E extends BaseEntity, R> {

    protected final Class<E> entity;
    private final String domain;
    protected Page<E> paginated;

    public AbstractBaseService(Class<E> klass) {
        entity = klass;
        domain = klass.getSimpleName();
    }

    public String getDomain() {
        return domain;
    }

    protected abstract BaseRepository<E> repository();

    protected Mapper<E, R> mapper() {
        return null;
    }

    public String user() {
        return AppUtil.connectedUser().orElse("system");
    }

    public void update(E entity) {
        repository().saveAndFlush(entity);
    }

    public E create(E entity) {
        return repository().saveAndFlush(entity);
    }

    public boolean delete(IdsRequest request) {
        repository().deleteAllById(request.ids());
        return true;
    }

    @Transactional
    public boolean delete(UuidsRequest request) {
        List<Integer> ids = getIds(request.ids());
        repository().deleteAllById(ids);
        return true;
    }

    private List<Integer> getIds(List<String> uuids) {
        return repository().getIds(uuids);
    }

    public boolean delete(Integer id) {
        repository().deleteById(id);
        return true;
    }

    public E find(Object id) {
        try {
            int iid = Integer.parseInt(id.toString());
            return repository().findById(iid).orElseThrow(() -> throwNotFound(id));
        } catch (NumberFormatException e) {
            return findByUuid(id.toString());
        }
    }

    public E findById(Integer id) {
        return find(id);
    }

    public E findByUuid(String uuid) {
        return repository().findByUuid(uuid).orElseThrow(() -> throwNotFound(uuid));
    }

    public E findByCode(String code) {
        return repository().findByCode(code).orElseThrow(() -> throwNotFound(code));
    }

    public R toResponse(Integer id) {
        return mapper().toResponse(findById(id));
    }

    public R toResponse(String uuid) {
        return mapper().toResponse(findByUuid(uuid));
    }

    public PageResponse<?> findAll() {
        return findAll(PaginationCriteria.of());
    }

    public PageResponse<?> findAll(PaginationCriteria criteria) {
        paginated = repository().findAll(criteria.pageable());
        return paginateResponse();
    }

    public PageResponse<?> searchByTerm(PaginationCriteria criteria) {
        String filter = criteria.filter() == null ? "" : criteria.filter();
        paginated = repository().findByLabelContaining(filter, criteria.pageable());
        return paginateResponse();
    }

    public Specification<E> applySpecification(List<FilterCriteria> filters) {
        return (new FilterSpecification<E>()).applyFilters(filters);
    }

    public PageResponse<?> applyFilters(List<FilterCriteria> filters, PaginationCriteria criteria) {
        paginated = repository().findAll(applySpecification(filters), criteria.pageable());
        return paginateResponse();
    }

    private EntityNotFoundException throwNotFound(Object data) {
        return new EntityNotFoundException(domain, data.toString());
    }

    protected PageResponse<?> paginateResponse() {
        if (mapper() == null) {
            log.error("THE MAPPER FOR THIS SERVICE IS NOT CONFIGURED.");
            return new PageResponse<>(List.of(), MetaResponse.of());
        }
        List<R> operationResponse = paginated.getContent()
                .stream()
                .map(e -> mapper().toResponse(e))
                .collect(Collectors.toList());

        return new PageResponse<>(operationResponse, MetaResponse.ofPage(paginated));
    }

    protected PageResponse<?> paginateResponse(Function<E, ?> mapperFunction) {
        List<?> operationResponse = paginated.getContent()
                .stream()
                .map(mapperFunction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageResponse<>(operationResponse, MetaResponse.ofPage(paginated));
    }
}
