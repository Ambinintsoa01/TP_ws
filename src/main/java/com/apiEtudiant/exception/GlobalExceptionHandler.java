package com.apiEtudiant.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.apiEtudiant.dto.ApiResponse;
import com.apiEtudiant.dto.ErrorDetails;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        ErrorDetails error = new ErrorDetails(ex.getErrorCode().name(), ex.getMessage(), null);
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(error));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(DataAccessException ex) {
        LOGGER.error("Erreur base de données", ex);
        ErrorDetails error = new ErrorDetails(
                ErrorCode.DATABASE_UNAVAILABLE.name(),
                "Impossible de contacter la base de données.",
                null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.error(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("field", err.getField());
                    map.put("message", err.getDefaultMessage());
                    return map;
                })
                .collect(Collectors.toList());

        ErrorDetails error = new ErrorDetails(ErrorCode.INVALID_REQUEST.name(), "Requête invalide.", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        LOGGER.error("Erreur imprévue", ex);
        ErrorDetails error = new ErrorDetails(
                ErrorCode.UNEXPECTED_ERROR.name(),
                "Une erreur imprévue est survenue.",
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error));
    }
}
