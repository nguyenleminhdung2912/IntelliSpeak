package com.gsu25se05.itellispeak.exception.auth;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthHandleException {

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Response> notLogin(Exception exception) {
        Response response = new Response(203, exception.getMessage(), null);
        return ResponseEntity.status(203).body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Response> accessDeniedException(AuthorizationDeniedException ex) {
        Response response = new Response(403, ex.getMessage(), null);
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(InvalidToken.class)
    public ResponseEntity<Response> invalidToken(InvalidToken ex) {
        Response response = new Response(400, ex.getMessage(), null);
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(AuthAppException.class)
    public ResponseEntity<Response> authAppException(AuthAppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        Response response = new Response(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
