package com.innowise.userservice.exception.handler;

import com.innowise.userservice.exception.AccountIsNotActivatedException;
import com.innowise.userservice.exception.BadIncomeDataException;
import com.innowise.userservice.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> internalServerError(Exception e){
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(
                        ErrorResponse.builder()
                        .message(e.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> entityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build()
                );
    }

    @ExceptionHandler(BadIncomeDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
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
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> accountIsNotActivated(AccountIsNotActivatedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                .body(
                        ErrorResponse.builder()
                        .message(e.getMessage())
                        .status(HttpStatus.FORBIDDEN.value())
                        .build());
    }
}
