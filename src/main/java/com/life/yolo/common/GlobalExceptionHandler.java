package com.life.yolo.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        // In production, log the error here
        e.printStackTrace(); 
        return ApiResponse.error(500, e.getMessage());
    }
}
