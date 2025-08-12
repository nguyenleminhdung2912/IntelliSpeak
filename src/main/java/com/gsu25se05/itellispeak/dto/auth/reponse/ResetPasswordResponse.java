package com.gsu25se05.itellispeak.dto.auth.reponse;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordResponse {
    private String message;
    private String error;
    private Integer code;
}
