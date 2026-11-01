package com.app.old_riders_bk.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Turns errors into readable JSON {"message": "..."} so the Flutter app can
 * show the driver what went wrong.
 *
 *  - IllegalArgumentException (e.g. "Email is already registered.")  -> 400
 *  - Bean validation failures (a field is missing or in the wrong format) -> 400
 *  - Unreadable JSON (a value of the wrong type, e.g. a bad date)      -> 400
 *  - IllegalStateException (e.g. "Ride cannot be started")            -> 409
 *  - Anything else                                                    -> 500, logged
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", Objects.toString(ex.getMessage(), "Invalid request.")));
    }

    /** Names the fields that failed, e.g. "registrationPlate: must not be blank". */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::describe)
                .collect(Collectors.joining("; "));
        String message = details.isBlank()
                ? "Some details are not valid. Please check the form."
                : "Please check: " + details;
        log.warn("Validation failed: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    /** A value has the wrong shape, for example a date not written as yyyy-MM-dd. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Request body could not be read: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Some details are in the wrong format. Please check the form and try again."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", Objects.toString(ex.getMessage(), "This action is not allowed right now.")));
    }

    /** Logs the real error on the server; the app only gets a safe sentence. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Something went wrong on our side. Please try again."));
    }

    private static String describe(FieldError e) {
        String msg = Objects.toString(e.getDefaultMessage(), "is not valid");
        return e.getField() + ": " + msg;
    }
}