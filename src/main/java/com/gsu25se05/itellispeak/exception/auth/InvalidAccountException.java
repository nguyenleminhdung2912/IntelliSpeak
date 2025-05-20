package com.gsu25se05.itellispeak.exception.auth;

public class InvalidAccountException extends RuntimeException {
    public InvalidAccountException(String message) {
        super(message);
    }
}
