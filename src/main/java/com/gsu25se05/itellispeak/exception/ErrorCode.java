package com.gsu25se05.itellispeak.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
//    ACCOUNTS | CODE: 1XXX
//    Accounts | Auth
    UNCATEGORIZED_EXCEPTION(1001, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "User name must be at least 3 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1004, "User not found", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID(1005, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(1006, "Invalid token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1021, "Token has expired", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_VERIFY(1007, "This account has not been verified", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(1008, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    OLD_PASSWORD_INCORRECT(1009, "Old password incorrect", HttpStatus.BAD_REQUEST),
    PASSWORD_REPEAT_INCORRECT(1010, "Password repeat do not match", HttpStatus.BAD_REQUEST),
    NOT_LOGIN(401, "You need to login", HttpStatus.BAD_REQUEST),
    USERNAME_PASSWORD_NOT_CORRECT(1012, "Email or password is not correct", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FOUND(404,"Account not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND(1013,"Email not found, please register account.", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_INSTRUCTOR(1014,"Account not instructor", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_STUDENT(1015,"Account not student", HttpStatus.BAD_REQUEST),
    ACCOUNT_IS_DELETED(1016,"This account has been deleted", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_APPROVED(1017, "Account not approved by Admin", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ADMIN(1018,"Account not admin", HttpStatus.BAD_REQUEST),
    NOT_PURCHASED_COURSE(1019, "Account has not purchased course", HttpStatus.BAD_REQUEST),
    UNEXPECTED_ERROR(1020, "An unexpected error occurred", HttpStatus.BAD_REQUEST),
//    Accounts | Emails | CODE: 15XX
    INVALID_OTP(1500, "Invalid email", HttpStatus.BAD_REQUEST),
    EMAIL_WAIT_VERIFY(1501, "This email has been registered and is not verified, please verify and login", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1502, "This email has been registered, please log in!", HttpStatus.BAD_REQUEST),
    ACCOUNT_ACCESS_FORBIDDEN(1503, "Access to this account is forbidden", HttpStatus.FORBIDDEN),

    SUCCESS(200, "Success",HttpStatus.OK),

//    TOPICS | CODE: 2XXX
    TOPICS_NOT_FOUND(2001, "Topics not found", HttpStatus.NOT_FOUND),
    INVALID_ENUM(2002, "Invalid enum format", HttpStatus.BAD_REQUEST),
    NO_COURSE_IN_WISHLIST(2003, "No courses in wishlist", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_ID(2004, "Invalid course ID", HttpStatus.BAD_REQUEST),
    COURSE_NOT_FOUND_IN_WISHLIST(2005, "Course not found in wishlist", HttpStatus.BAD_REQUEST),
    COURSE_IS_DELETED(2006,"This Course has been deleted", HttpStatus.BAD_REQUEST),
    COURSE_IS_UNABLE_TO_SAVE(2008,"This Course is unable to be saved", HttpStatus.CONFLICT),
    INVALID_PRICE(2007, "Invalid price", HttpStatus.BAD_REQUEST),
    FILE_MAX_SIZE(2008, "Maximum file size exceeded", HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE),

// BILLING | CODE 8XXX
    BILLING_NOT_EMPTY(7001, "Billing is not empty", HttpStatus.BAD_REQUEST),
    BILLING_NOT_FOUNT(7002, "Billing not found, please re-check", HttpStatus.NOT_FOUND),

    ;
    @Getter
    private final Integer code;
    @Setter
    private String message;
    @Getter
    private final HttpStatus httpStatus;

    ErrorCode(Integer code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
