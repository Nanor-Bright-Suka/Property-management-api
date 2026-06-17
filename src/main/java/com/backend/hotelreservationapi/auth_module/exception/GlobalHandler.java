package com.backend.hotelreservationapi.auth_module.exception;


import com.backend.hotelreservationapi.auth_module.util.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalHandler{


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        400,
                        message,
                        request.getRequestURI()


                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonParseError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        400,
                        "Malformed JSON request",
                        request.getRequestURI()

                ));
    }


    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiErrorResponse(429, ex.getMessage(), null));
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
