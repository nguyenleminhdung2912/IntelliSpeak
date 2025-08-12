package com.gsu25se05.itellispeak.dto.auth.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponseDTO {
    private String message;
    private String error;
    private Integer code;
    private String data;

    public RegisterResponseDTO(Integer code, String message, String error) {
        this.code = code;
        this.message = message;
        this.error = error;
    }

    public RegisterResponseDTO(String message, String error, Integer code, String data) {
        super();
        this.message = message;
        this.error = error;
        this.code = code;
        this.data = data;
    }
}