package com.freightfox.tollplaza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.freightfox.tollplaza.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPincodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPincode(InvalidPincodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(SamePincodeException.class)
    public ResponseEntity<ErrorResponse> handleSamePincode(SamePincodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String defaultErrorMsg = "Invalid source or destination pincode";
        if (ex.getBindingResult().getFieldError() != null) {
            String fieldMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
            if (fieldMessage != null && fieldMessage.contains("Indian pincode")) {
                defaultErrorMsg = "Invalid source or destination pincode";
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(defaultErrorMsg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An internal server error occurred: " + ex.getMessage()));
    }
}
