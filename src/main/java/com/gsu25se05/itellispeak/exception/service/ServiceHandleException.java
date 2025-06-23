package com.gsu25se05.itellispeak.exception.service;

import com.gsu25se05.itellispeak.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ServiceHandleException {

    @ExceptionHandler(CreateServiceException.class)
    public ResponseEntity<?> CreateServiceException(CreateServiceException createServiceException) {
        Response response = new Response(403, createServiceException.getMessage(), null);
        return ResponseEntity.status(403).body(response);
    }

}
