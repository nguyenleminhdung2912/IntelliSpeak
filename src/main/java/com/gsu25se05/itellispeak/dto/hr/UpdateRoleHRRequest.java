package com.gsu25se05.itellispeak.dto.hr;

import com.gsu25se05.itellispeak.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleHRRequest {
    private User.Role role;
    private Long companyId;
}
