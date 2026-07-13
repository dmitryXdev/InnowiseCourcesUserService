package com.innowise.userservice.exception.handler;

import com.innowise.userservice.exception.AccessDeniedException;
import com.innowise.userservice.exception.AccountIsNotActivatedException;
import com.innowise.userservice.exception.BadIncomeDataException;
import com.innowise.userservice.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> internalServerError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .build()
                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.NOT_FOUND.value())
                                .build()
                );
    }

    @ExceptionHandler(BadIncomeDataException.class)
    public ResponseEntity<ErrorResponse> badIncomeData(BadIncomeDataException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build()
                );
    }

    @ExceptionHandler(AccountIsNotActivatedException.class)
    public ResponseEntity<ErrorResponse> accountIsNotActivated(AccountIsNotActivatedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.FORBIDDEN.value())
                                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> badIncomeData(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build()
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> badIncomeData(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.FORBIDDEN.value())
                                .build()
                );
    }
}
