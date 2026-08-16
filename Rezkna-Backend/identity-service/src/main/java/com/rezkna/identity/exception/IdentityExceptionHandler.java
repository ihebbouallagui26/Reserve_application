package com.rezkna.identity.exception;

import com.rezkna.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles the status codes common-lib's GlobalExceptionHandler does not cover (401,
 * 403, 409, 429).
 * <p>
 * AuthenticationException/AccessDeniedException are handled explicitly here rather
 * than left to Spring Security's JwtAuthenticationEntryPoint/RestAccessDeniedHandler:
 * those only fire for exceptions that reach ExceptionTranslationFilter, but
 * GlobalExceptionHandler's own catch-all {@code @ExceptionHandler(Exception.class)}
 * intercepts controller-thrown exceptions first (Spring MVC's exception resolution
 * runs inside the filter chain, before the exception would otherwise propagate back
 * out to it) - so without an explicit, more-specific handler here, a wrong-token-type
 * or OWNER-only rejection would incorrectly surface as a 500. The messages match
 * JwtAuthenticationEntryPoint/RestAccessDeniedHandler exactly so a request rejected at
 * the filter level (no token) and one rejected inside a controller (wrong token type)
 * are indistinguishable to the client, by design.
 */
@RestControllerAdvice
public class IdentityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Missing or invalid authentication token"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyAttempts(TooManyAttemptsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.error(ex.getMessage()));
    }
}
