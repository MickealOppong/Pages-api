package com.pages.util;


import com.pages.exception.InsufficientPublicPresenceException;
import com.pages.exception.InvalidOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientPublicPresenceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientPublicPresence(InsufficientPublicPresenceException ex) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("status", HttpStatus.FORBIDDEN.value()); // 403 Forbidden or HttpStatus.BAD_REQUEST (400)
        body.put("error", "Feature Locked");
        body.put("message", ex.getMessage());

        // Return a clean, structured JSON object with the right HTTP code instead of a 500 error
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validationException(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
       Map<String,String> errors = ex.getBindingResult().getFieldErrors().stream()
                        .collect(Collectors.toMap(FieldError::getField, fieldError -> fieldError.getDefaultMessage()!=null?
                                fieldError.getDefaultMessage():"Invalid value",(existingMessage,newMessage)->existingMessage+","+newMessage));
        body.put("status", HttpStatus.FORBIDDEN.value()); // 403 Forbidden or HttpStatus.BAD_REQUEST (400)
        body.put("error", errors);
        body.put("message", "One or more field errors");

        // Return a clean, structured JSON object with the right HTTP code instead of a 500 error
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperationException(InvalidOperationException ex) {
       InvalidOperationException error = new InvalidOperationException(ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.CONFLICT); // 403 Forbidden or HttpStatus.BAD_REQUEST (400)
        body.put("error",error.getMessage());
        body.put("message", "An error occurred");
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperationException(Exception ex) {
        InvalidOperationException error = new InvalidOperationException(ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST); // 403 Forbidden or HttpStatus.BAD_REQUEST (400)
        body.put("error",error.getMessage());
        body.put("message", "An error occurred");
        return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
    }


}

