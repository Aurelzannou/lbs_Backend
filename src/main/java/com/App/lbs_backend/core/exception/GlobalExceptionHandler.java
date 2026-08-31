package com.App.lbs_backend.core.exception;

import com.App.lbs_backend.core.http.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Intercepte toutes les exceptions applicatives et renvoie
 * un ApiResponse standardisé avec le bon code HTTP.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateCodeException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateCode(DuplicateCodeException ex, HttpServletRequest request) {
        log.warn("Code déjà utilisé sur {} : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.apiError(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Entité introuvable sur {} : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.apiError(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Erreur de validation sur {} : {}", request.getRequestURI(), errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.apiError(errors, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        // Ce cas couvre toutes les règles métier (verrous de saisie, chevauchements de dates,
        // autorisations professeur, etc.) — on log toujours le message pour pouvoir diagnostiquer
        // sans deviner : avant, rien n'était loggé ici, rendant ces erreurs invisibles en console.
        log.warn("Requête refusée sur {} : {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.apiError(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public void handleUnsupported(UnsupportedOperationException ex,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("Opération non supportée sur {} : {}", request.getRequestURI(), ex.getMessage());
        ecrireErreur(response, HttpStatus.NOT_IMPLEMENTED,
                "Cette opération n'est pas encore disponible.", request);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public void handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warn("Contrainte d'intégrité sur {} : {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        ecrireErreur(response, HttpStatus.CONFLICT,
                "Impossible : des données sont encore rattachées à cet élément.", request);
    }

    @ExceptionHandler(Exception.class)
    public void handleGenericException(Exception ex,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("Erreur non gérée sur {} : ", request.getRequestURI(), ex);
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        ecrireErreur(response, HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue : " + msg, request);
    }

    /**
     * Écrit le corps d'erreur JSON directement dans la réponse HTTP, sans passer par la
     * négociation de contenu ni les HttpMessageConverter. Dans ce projet (Spring Boot 4,
     * Jackson 3 + Jackson 2 en transitif, spring-data-rest + hateoas), la sérialisation
     * d'un ResponseEntity depuis un @RestControllerAdvice échoue de façon intermittente
     * ("No converter" / "No acceptable representation") — l'écriture manuelle est fiable.
     */
    private void ecrireErreur(HttpServletResponse response, HttpStatus status,
            String message, HttpServletRequest request) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = "{\"success\":false,\"message\":\"" + echapper(message)
                + "\",\"data\":null,\"path\":\"" + echapper(request.getRequestURI()) + "\"}";
        response.getWriter().write(json);
    }

    /** Échappe les caractères qui casseraient le JSON produit à la main. */
    private String echapper(String valeur) {
        if (valeur == null) return "";
        StringBuilder sb = new StringBuilder(valeur.length() + 16);
        for (int i = 0; i < valeur.length(); i++) {
            char c = valeur.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
