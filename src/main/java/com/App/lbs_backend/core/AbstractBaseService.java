package com.App.lbs_backend.core;

import com.App.lbs_backend.core.exception.DuplicateCodeException;
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
import org.jboss.logging.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractBaseService<E, R> {

    protected static final Logger logger = Logger.getLogger(AbstractBaseService.class);
    protected final Class<E> entity;
    private final String domain;

    public AbstractBaseService(Class<E> klass) {
        entity = klass;
        domain = klass.getSimpleName();
    }

    public String getDomain() {
        return domain;
    }

    public abstract BaseRepository<E> repository();

    public Mapper<E, R> mapper() {
        return null;
    }

    public String user() {
        return AppUtil.connectedUser().orElse("spring");
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
        List<Long> ids = getIds(request.ids());
        repository().deleteAllById(ids);
        return true;
    }

    private List<Long> getIds(List<String> uuids) {
        return repository().getIds(uuids);
    }

    public boolean delete(Long id) {
        repository().deleteById(id);
        return true;
    }

    public E find(Object id) {
        try {
            Long iid = Long.parseLong(id.toString());
            return repository().findById(iid).orElseThrow(() -> throwNotFound(id));
        } catch (NumberFormatException e) {
            return findByUuid(id.toString());
        }
    }

    public E findById(Long id) {
        return find(id);
    }

    public E findByUuid(String uuid) {
        return repository().findByUuid(uuid).orElseThrow(() -> throwNotFound(uuid));
    }

    public E findByCode(String code) {
        return repository().findByCode(code).orElseThrow(() -> throwNotFound(code));
    }

    public void checkCodeUniqueness(String code) {
        if (code != null && !code.isEmpty() && repository().existsByCode(code)) {
            throw new DuplicateCodeException(domain, code);
        }
    }

    public void checkCodeUniqueness(String code, String uuid) {
        if (code == null || code.isEmpty()) return;
        
        repository().findByStrictCode(code).ifPresent(existing -> {
            if (!uuid.equals(getUuidFromEntity(existing))) {
                throw new DuplicateCodeException(domain, code);
            }
        });
    }

    /**
     * Extrait l'UUID d'une entité en utilisant l'interface Timestamps
     * que toutes les entités implémentent.
     */
    private String getUuidFromEntity(E entity) {
        if (entity instanceof Timestamps timestamps) {
            return timestamps.getUuid();
        }
        return "";
    }

    public R toResponse(Long id) {
        return mapper().toResponse(findById(id));
    }

    public R toResponse(String uuid) {
        return mapper().toResponse(findByUuid(uuid));
    }

    public PageResponse<?> findAll() {
        return findAll(PaginationCriteria.of());
    }

    @SuppressWarnings("unused")
    public PageResponse<?> customPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<E> list = repository().findAll();

        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), list.size());

        Page<E> paginated;
        if (!list.isEmpty()) {
            paginated = new PageImpl<>(list.subList(start, end), pageable, list.size());
        } else {
            paginated = new PageImpl<>(list, pageable, 0);
        }
        return paginateResponse(paginated);
    }

    public PageResponse<?> findAll(PaginationCriteria criteria) {
        Page<E> paginated = repository().findAll(criteria.pageable());
        return paginateResponse(paginated);
    }

    public PageResponse<?> searchByTerm(PaginationCriteria criteria) {
        String filter = criteria.filter() == null ? "" : criteria.filter();
        Page<E> paginated = repository().findByLabelContaining(filter, criteria.pageable());
        return paginateResponse(paginated);
    }

    public Specification<E> applySpecification(List<FilterCriteria> filters) {
        return (new FilterSpecification<E>()).applyFilters(filters);
    }

    public PageResponse<?> applyFilters(List<FilterCriteria> filters, PaginationCriteria criteria) {
        Page<E> paginated = repository().findAll(applySpecification(filters), criteria.pageable());
        return paginateResponse(paginated);
    }

    private EntityNotFoundException throwNotFound(Object data) {
        return new EntityNotFoundException(domain, data.toString());
    }

    protected PageResponse<?> paginateResponse(Page<E> paginated) {
        if (mapper() == null) {
            logger.error("THE MAPPER FOR THIS SERVICE IS NOT CONFIGURED.");
            return new PageResponse<>(List.of(), MetaResponse.of());
        }
        List<R> operationResponse = paginated.getContent()
                .stream()
                .map(mapper()::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(operationResponse, MetaResponse.ofPage(paginated));
    }

    protected PageResponse<?> paginateResponse(Page<E> paginated, Function<E, ?> mapperFunction) {
        List<?> operationResponse = paginated.getContent()
                .stream()
                .map(mapperFunction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageResponse<>(operationResponse, MetaResponse.ofPage(paginated));
    }

    public Object dataForForm() {
        return null;
    }

    public Page<E> getPlainEntities(PaginationCriteria criteria) {
        return repository().findAll(criteria.pageable());
    }
}
