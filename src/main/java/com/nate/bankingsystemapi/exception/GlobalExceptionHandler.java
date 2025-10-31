package com.nate.bankingsystemapi.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record ApiError(Instant timestamp,int status,String error,String message, String path){}

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlesAccountNotFound(AccountNotFoundException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),404,"Account Not Found",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlesUserNotFound(UserNotFoundException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),404,"User Not Found",ex.getMessage(),req.getRequestURI());
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handlesAccessDenied(AccessDeniedException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),403,"Forbidden",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handlesJwtException(JwtException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),401,"Unauthorized",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, jakarta.servlet.http.HttpServletRequest req){
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e-> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Failed Validation");

        return new ApiError(Instant.now(),400,"Bad Request",msg, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentTypeMismatchException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),400,"Bad Request",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex,jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),400,"Bad Request",ex.getMessage(),req.getRequestURI());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),500, "Internal Server Error", ex.getMessage(), req.getRequestURI());
    }
}
