package com.App.lbs_backend.core;

import com.App.lbs_backend.core.annotation.CreateResource;
import com.App.lbs_backend.core.annotation.UpdateResource;
import com.App.lbs_backend.core.http.request.FormRequest;
import com.App.lbs_backend.core.http.request.IdsRequest;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.specs.FilterCriteria;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.App.lbs_backend.core.http.response.ApiResponse.apiSuccess;

public abstract class MasterController<E extends BaseEntity, R, F extends FormRequest> {

    @Autowired
    protected HttpServletRequest request;

    protected abstract AbstractBaseService<E, R> service();

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Validated(CreateResource.class) F form) {
        return sendCreateResponse(doCreate(form));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable("uuid") String uuid,
                                    @RequestBody @Validated(UpdateResource.class) F form) {
        R response = doUpdate(uuid, form);
        return sendUpdateResponse(response, updateMessage(uuid));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable("uuid") String uuid) {
        return sendDeleteResponse(doDelete(UuidsRequest.of(uuid)));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> massDelete(@RequestBody IdsRequest ids) {
        return sendDeleteResponse(service().delete(ids));
    }

    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        return sendResponse(service().searchByTerm(criteria));
    }

    @PostMapping("/filter")
    public ResponseEntity<?> filter(PaginationCriteria criteria, @RequestBody List<FilterCriteria> filters) {
        return sendResponse(service().applyFilters(filters, criteria), MessageConstants.DATA_FILTERED);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<?> findOne(@PathVariable("identifier") String identifier) {
        try {
            return sendResponse(get(Integer.parseInt(identifier)), String.format(MessageConstants.ENTRY_FOUND, identifier));
        } catch (NumberFormatException e) {
            return sendResponse(get(identifier), String.format(MessageConstants.ENTRY_FOUND, identifier));
        }
    }

    protected R get(Integer id) {
        return service().toResponse(id);
    }

    protected R get(String code) {
        return service().toResponse(code);
    }

    protected R doCreate(F form) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    protected R doUpdate(String id, F form) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    protected boolean doDelete(UuidsRequest uuids) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    // Utility Methods for Response Handling
    private <X> ResponseEntity<?> sendCreateResponse(X data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiSuccess(MessageConstants.ENTRY_CREATED, data, path()));
    }

    private <X> ResponseEntity<?> sendUpdateResponse(X data, String message) {
        return ResponseEntity.accepted().body(apiSuccess(message, data, path()));
    }

    private <X> ResponseEntity<?> sendResponse(X data, String message) {
        return ResponseEntity.ok(apiSuccess(message, data, path()));
    }

    protected  <X> ResponseEntity<?> sendResponse(X data) {
        return sendResponse(data, MessageConstants.DATA_RETRIEVED);
    }

    private ResponseEntity<?> sendDeleteResponse(boolean deleted) {
        return sendResponse(deleted, MessageConstants.ENTRY_REMOVED);
    }

    private String updateMessage(Object data) {
        return "Mise à jour réussie pour " + data;
    }

    protected String path() {
        return request.getRequestURI();
    }
}
