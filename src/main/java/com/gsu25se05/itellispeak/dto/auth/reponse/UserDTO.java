package com.gsu25se05.itellispeak.dto.auth.reponse;

import com.gsu25se05.itellispeak.entity.PlanType;
import com.gsu25se05.itellispeak.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private User.Role role;
    private PlanType planType;
    private LocalDate birthday;
    private String avatar;
    private String status;
    private String bio;
    private String website;
    private String github;
    private String linkedin;
    private String facebook;
    private String youtube;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean isDeleted;
}
