package com.gsu25se05.itellispeak.dto.auth.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordRequest {
    private String new_password;
    private String repeat_password;
}
