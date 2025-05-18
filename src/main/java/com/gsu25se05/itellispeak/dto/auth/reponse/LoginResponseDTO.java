package com.gsu25se05.itellispeak.dto.auth.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gsu25se05.itellispeak.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    private int code;

    private String message;

    private String error;

    private User.Role role;

    private String accessToken;

    private String refreshToken;

    public LoginResponseDTO(int code, String message, String error, User.Role role, String accessToken, String refreshToken) {
        super();
        this.code = code;
        this.message = message;
        this.error = error;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}

