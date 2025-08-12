package com.gsu25se05.itellispeak.dto.admin;

import lombok.Data;

import java.util.UUID;

@Data
public class UserWithPackageDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private String status;
    private String packageName;
    private Long packageId;
}
