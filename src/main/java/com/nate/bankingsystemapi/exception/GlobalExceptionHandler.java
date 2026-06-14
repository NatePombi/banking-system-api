package com.nate.bankingsystemapi.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record ApiError(Instant timestamp,int status,String error,String message, String path){}

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlesAccountNotFound(AccountNotFoundException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.NOT_FOUND.value(), "Account Not Found",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlesUserNotFound(UserNotFoundException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.NOT_FOUND.value(), "User Not Found",ex.getMessage(),req.getRequestURI());
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handlesAccessDenied(AccessDeniedException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.FORBIDDEN.value(), "Forbidden",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handlesJwtException(JwtException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unauthorized",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleValidation(MethodArgumentNotValidException ex, jakarta.servlet.http.HttpServletRequest req){
        String msg = ex.getBindingResult().getAllErrors().stream()
                .map(e-> {
                    if(e instanceof FieldError fieldError){
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return e.getObjectName() + ": " + e.getDefaultMessage();
                })
                .collect(Collectors.joining());

        if(msg.isBlank()){
            msg = "Validation Failed";
        }


        return new ApiError(Instant.now(),HttpStatus.UNPROCESSABLE_ENTITY.value(), "unprocessable Entity",msg, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentTypeMismatchException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.BAD_REQUEST.value(),"Bad Request",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex,jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.BAD_REQUEST.value(),"Bad Request",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(DuplicateRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDuplicate(DuplicateRequestException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.BAD_REQUEST.value(), "Duplicate Request",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(UsernameExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlesDuplicateUsername(UsernameExistsException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.CONFLICT.value(),"Conflict", ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), req.getRequestURI());
    }


}
