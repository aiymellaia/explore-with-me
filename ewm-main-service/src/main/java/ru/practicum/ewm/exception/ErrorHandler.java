package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        return ApiError.builder()
                .status("NOT_FOUND")
                .reason("The required object was not found.")
                .message(e.getMessage())
                .errors(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final Exception e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .errors(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(final ValidationException e) {
        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .errors(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message("Validation failed")
                .errors(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        return ApiError.builder()
                .status("INTERNAL_SERVER_ERROR")
                .reason("An unexpected error occurred.")
                .message(e.getMessage())
                .errors(Collections.singletonList(e.toString()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ApiError.builder()
                .errors(List.of(e.getMessage()))
                .message(e.getMessage())
                .reason("Invalid JSON format or structure.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath().toString() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message("Validation failed: " + e.getMessage()) // Общее описание
                .errors(errors) // Список конкретных нарушений
                .timestamp(LocalDateTime.now())
                .build();
    }
}