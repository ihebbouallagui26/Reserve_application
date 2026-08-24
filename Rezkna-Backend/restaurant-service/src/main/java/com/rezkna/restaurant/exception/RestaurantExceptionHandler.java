package com.rezkna.restaurant.exception;

import com.rezkna.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles the status codes common-lib's GlobalExceptionHandler does not cover (401, 403)
 * for exceptions thrown manually inside RestaurantController (via PlatformTokenVerifier/
 * requirePlatformOwner), not during the Spring Security filter chain. Mirrors identity-
 * service's IdentityExceptionHandler for exactly the two cases restaurant-service needs -
 * see that class's Javadoc for why a more specific handler is required here at all
 * (GlobalExceptionHandler's catch-all would otherwise turn these into a 500).
 */
@RestControllerAdvice
public class RestaurantExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Missing or invalid authentication token"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
    }
}
