package com.ecommerce.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> body(HttpStatus s, Object msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", LocalDateTime.now().toString());
        m.put("status", s.value());
        m.put("error", s.getReasonPhrase());
        m.put("message", msg);
        return ResponseEntity.status(s).body(m);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> api(ApiException e) { return body(e.getStatus(), e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        Map<String, String> errs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errs.put(f.getField(), f.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, errs);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "Malformed JSON request body");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException e) {
        return body(HttpStatus.CONFLICT, "Operation violates a data constraint (duplicate value or record is in use)");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> other(Exception e) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
