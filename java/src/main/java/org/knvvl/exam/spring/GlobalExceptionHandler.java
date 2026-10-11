package org.knvvl.exam.spring;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler that converts unhandled exceptions into a consistent
 * JSON error body.  The shape mirrors Spring Boot's default {@code /error}
 * response so that clients need only one error-parsing path:
 *
 * <pre>
 * {
 *   "status"  : 500,
 *   "error"   : "Internal Server Error",
 *   "message" : "&lt;exception message&gt;"
 * }
 * </pre>
 *
 * <p>Existing {@code try/catch} blocks inside REST controllers are left
 * untouched; this handler only fires when an exception propagates out of a
 * controller method without being caught locally.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String FALLBACK_MESSAGE = "No message available";

    /**
     * Handles Spring's {@link ResponseStatusException}, preserving the HTTP
     * status code and reason phrase that the thrower intended.
     *
     * <p>4xx responses are logged at WARN without a stack trace (client errors).
     * 5xx responses are logged at ERROR with full stack trace (server errors).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        int statusValue = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(statusValue);
        String errorPhrase = status != null ? status.getReasonPhrase() : ex.getStatusCode().toString();
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        String safeMessage = message != null ? message : FALLBACK_MESSAGE;

        if (statusValue >= 500) {
            LOGGER.error("ResponseStatusException: {} {}", statusValue, safeMessage, ex);
        } else {
            LOGGER.warn("ResponseStatusException: {} {}", statusValue, safeMessage);
        }

        return ResponseEntity
            .status(ex.getStatusCode())
            .body(buildBody(statusValue, errorPhrase, safeMessage));
    }

    /**
     * Catch-all handler for any other unhandled exception.  Returns HTTP 500
     * with the exception message in the {@code message} field.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        String safeMessage = ex.getMessage() != null ? ex.getMessage() : FALLBACK_MESSAGE;

        LOGGER.error("Unhandled exception: {}", safeMessage, ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                safeMessage));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Map<String, Object> buildBody(int status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}