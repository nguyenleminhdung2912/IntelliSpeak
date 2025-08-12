package com.gsu25se05.itellispeak.exception.auth;

import com.gsu25se05.itellispeak.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthAppException extends RuntimeException{
    private ErrorCode errorCode;

    public AuthAppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
