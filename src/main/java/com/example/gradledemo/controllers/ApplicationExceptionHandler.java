package com.example.gradledemo.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;


// Этот хендлер нужен в одном случае - если аннотация RequestBody не сможет преобразовать тело
// запроса в объект SystemItemImportRequest
@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 400);
        map.put("message", "Validation Failed");
        return ResponseEntity.status(400).body(map);
    }
}
