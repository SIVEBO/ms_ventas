package com.sivebo.ms_ventas.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
                Map<String, String> errores = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                        .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errores);
        }

        @ExceptionHandler(MicroserviceValidationException.class)
        public ResponseEntity<Map<String, String>> handleValidationException(MicroserviceValidationException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", ex.getMessage()));
        }

        @ExceptionHandler(MicroserviceUnavailableException.class)
        public ResponseEntity<Map<String, String>> handleUnavailableException(MicroserviceUnavailableException ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", ex.getMessage()));
        }

        @ExceptionHandler(MicroserviceForbiddenException.class)
        public ResponseEntity<Map<String, String>> handleForbiddenException(MicroserviceForbiddenException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", ex.getMessage()));
        }
}
