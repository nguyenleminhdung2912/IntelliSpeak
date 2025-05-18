package com.gsu25se05.itellispeak.dto.auth.request;

import com.gsu25se05.itellispeak.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private User.Role role;
}
