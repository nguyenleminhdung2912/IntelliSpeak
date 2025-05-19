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


    public LoginResponseDTO(int code, String message, String error) {
        super();
        this.code = code;
        this.message = message;
        this.error = error;
    }
}

