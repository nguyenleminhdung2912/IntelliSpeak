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
    public ResponseEntity<?> accessDeniedException(AuthorizationDeniedException authorizationDeniedException) {
        Response response = new Response(403, authorizationDeniedException.getMessage(), null);
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(InvalidToken.class)
    public ResponseEntity invalidToken(InvalidToken invalidToken) {
        Response response = new Response(400, invalidToken.getMessage(), null);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(AuthAppException.class)
    public ResponseEntity authAppException(ErrorCode errorCode) {
        Response response = new Response(400, errorCode.getMessage(), null);
        return ResponseEntity.ok(response);
    }
}
