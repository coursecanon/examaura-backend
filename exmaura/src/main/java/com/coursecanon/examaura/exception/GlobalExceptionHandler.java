//package com.coursecanon.examaura.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
//            MethodArgumentNotValidException ex) {
//
//        List<ValidationFieldError> errors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(error -> new ValidationFieldError(
//                        error.getField(),
//                        error.getDefaultMessage()
//                ))
//                .collect(Collectors.toList());
//
//        ValidationErrorResponse response = new ValidationErrorResponse(
//                ZonedDateTime.now(),
//                HttpStatus.BAD_REQUEST.value(),
//                "Validation Failed",
//                "Invalid request data",
//                errors
//        );
//
//        return ResponseEntity.badRequest().body(response);
//    }
//}