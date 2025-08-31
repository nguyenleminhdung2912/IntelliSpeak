package com.gsu25se05.itellispeak.dto.auth.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordResponse {
    private String message;
    private String error;
    private int code;
}
