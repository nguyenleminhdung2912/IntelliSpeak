package com.gsu25se05.itellispeak.dto.auth.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    private int code;

    private String message;

    private String error;


    private String token;
    private String refreshToken;
    private UserDTO user;


    public LoginResponseDTO(int code, String message, String error) {
        super();
        this.code = code;
        this.message = message;
        this.error = error;
    }
}

