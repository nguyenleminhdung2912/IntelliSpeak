package com.gsu25se05.itellispeak.exception.auth;

public class InvalidToken extends RuntimeException{
    public InvalidToken(String message) {
        super(message);
    }
}