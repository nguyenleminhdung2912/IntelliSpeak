package com.gsu25se05.itellispeak.exception.auth;

public class NotLoginException extends RuntimeException{
    public NotLoginException(String message){
        super(message);
    }
}
