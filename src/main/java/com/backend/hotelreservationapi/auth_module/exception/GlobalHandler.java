package com.backend.hotelreservationapi.auth_module.exception;


import com.backend.hotelreservationapi.auth_module.util.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GlobalHandler{
    private static final String MESSAGE = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethoArgumentNotValidExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    String message = "typeMismatch".equals(error.getCode())
                            ? "Invalid " + error.getField()
                            : error.getDefaultMessage();

                    Map<String, String> errorMap = new LinkedHashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put(MESSAGE, message != null ? message : "Invalid value");
                    return errorMap;
                })
                .toList();
        Map<String, Object> body = new HashMap<>();
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        Map<String, Object> errorDetails = new LinkedHashMap<>();

        errorDetails.put("timestamp", Instant.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Bad Request");

        if ("applicationId".equals(ex.getName())) {
            errorDetails.put(MESSAGE, "Invalid ID.");
        } else {
            errorDetails.put(MESSAGE, "Invalid value provided for parameter: " + ex.getName());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiErrorResponse(403, ex.getMessage(), Instant.now().toString()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonParseError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(400, "Malformed JSON request", request.getRequestURI()));
    }


    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiErrorResponse(429, ex.getMessage(), null));
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleFieldValidationException(FieldValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(new ApiErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), null));
    }


    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
        return ResponseEntity.status(401).body(new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(404).body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));

    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleFileStorageException(FileStorageException ex) {
        return ResponseEntity.status(500).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadToken(IllegalArgumentException ex) {
        return ResponseEntity.status(400)
                .body(new ApiErrorResponse(400, "malformed token", null));
    }
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleGenericJwt(JwtException ex) {
        return ResponseEntity.status(401).body(new ApiErrorResponse(401, "invalid or token expired", null));
    }


    @ExceptionHandler(FileTypeValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleFileTypeException(FileTypeValidationException ex) {
        return ResponseEntity.status(500).body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(500).body(new ApiErrorResponse(500, "Internal server error", request.getRequestURI()
        ));
    }



















}
