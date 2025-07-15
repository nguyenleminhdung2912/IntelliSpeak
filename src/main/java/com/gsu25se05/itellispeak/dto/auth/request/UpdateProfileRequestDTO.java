package com.gsu25se05.itellispeak.dto.auth.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileRequestDTO {
    private String firstName;
    private String lastName;
    private String bio;
    private String avatar;
    private String website;
    private String github;
    private String linkedin;
    private String facebook;
    private String youtube;
}
