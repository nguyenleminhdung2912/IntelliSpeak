package com.gsu25se05.itellispeak.dto.admin;

import com.gsu25se05.itellispeak.entity.User;
import lombok.Data;

@Data
public class CreateUserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private User.Role role;
}
