package com.gsu25se05.itellispeak.dto.admin;

import com.gsu25se05.itellispeak.entity.User;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    private User.Role role;
}
