package com.gsu25se05.itellispeak.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // ACCOUNTS | CODE: 1XXX
    UNCATEGORIZED_EXCEPTION(1001, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User already exists", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1004, "User not found", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID(1005, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(1006, "Invalid token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1021, "Token has expired", HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_VERIFY(1007, "Account is not verified", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(1008, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    OLD_PASSWORD_INCORRECT(1009, "Old password is incorrect", HttpStatus.BAD_REQUEST),
    PASSWORD_REPEAT_INCORRECT(1010, "Repeated password does not match", HttpStatus.BAD_REQUEST),
    NOT_LOGIN(401, "You need to log in", HttpStatus.UNAUTHORIZED),
    USERNAME_PASSWORD_NOT_CORRECT(1012, "Email or password is incorrect", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FOUND(404, "Account not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND(1013, "Email not found, please register", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_INSTRUCTOR(1014, "Account is not an instructor", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_STUDENT(1015, "Account is not a student", HttpStatus.BAD_REQUEST),
    ACCOUNT_IS_DELETED(1016, "This account has been deleted", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_APPROVED(1017, "Account not approved by admin", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ADMIN(1018, "Account is not an admin", HttpStatus.BAD_REQUEST),
    NOT_PURCHASED_COURSE(1019, "Account has not purchased the course", HttpStatus.BAD_REQUEST),
    UNEXPECTED_ERROR(1020, "An unexpected error occurred", HttpStatus.BAD_REQUEST),

    // ACCOUNTS | EMAILS | CODE: 15XX
    INVALID_OTP(1500, "Invalid OTP", HttpStatus.BAD_REQUEST),
    EMAIL_WAIT_VERIFY(1501, "Email is registered; please verify and log in", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1502, "This email has been registered; please log in", HttpStatus.BAD_REQUEST),
    ACCOUNT_ACCESS_FORBIDDEN(1503, "Access to this account is forbidden", HttpStatus.FORBIDDEN),

    // ACCOUNTS | HR | CODE: 16XX
    HR_ALREADY_APPLIED(1600, "You have already submitted a request to become HR", HttpStatus.CONFLICT),
    HR_NOT_FOUND(1601, "You have not submitted a request to become HR", HttpStatus.NOT_FOUND),

    SUCCESS(200, "Success", HttpStatus.OK),

    // TOPICS | CODE: 2XXX
    TOPICS_NOT_FOUND(2001, "Topics not found", HttpStatus.NOT_FOUND),
    INVALID_ENUM(2002, "Invalid enum format", HttpStatus.BAD_REQUEST),
    NO_COURSE_IN_WISHLIST(2003, "No courses in wishlist", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_ID(2004, "Invalid course ID", HttpStatus.BAD_REQUEST),
    COURSE_NOT_FOUND_IN_WISHLIST(2005, "Course not found in wishlist", HttpStatus.BAD_REQUEST),
    COURSE_IS_DELETED(2006, "This course has been deleted", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(2007, "Invalid price", HttpStatus.BAD_REQUEST),
    COURSE_IS_UNABLE_TO_SAVE(2008, "This course cannot be saved", HttpStatus.CONFLICT),
    FILE_MAX_SIZE(2009, "Maximum file size exceeded", HttpStatus.PAYLOAD_TOO_LARGE),

    // INTERVIEW | CODE: 3XXX
    OUT_OF_INTERVIEW_COUNT(3001, "You have reached the limit for virtual interviews", HttpStatus.FORBIDDEN),

    // INTERVIEW SESSION | CODE: 4XXX
    INTERVIEW_SESSION_WERE_DELETED(3001, "You have reached the limit for virtual interviews", HttpStatus.FORBIDDEN),

    // CV & JD | CODE: 4XXX
    OUT_OF_CV_ANALYZE_COUNT(4001, "You have reached the limit for CV analysis", HttpStatus.FORBIDDEN),
    OUT_OF_JD_ANALYZE_COUNT(4002, "You have reached the limit for JD analysis", HttpStatus.FORBIDDEN),

    // BILLING | CODE: 7XXX
    BILLING_NOT_EMPTY(7001, "Billing is not empty", HttpStatus.BAD_REQUEST),
    BILLING_NOT_FOUND(7002, "Billing not found, please re-check", HttpStatus.NOT_FOUND);

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
