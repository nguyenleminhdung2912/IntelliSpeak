package com.gsu25se05.itellispeak.dto.auth.reponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDTO {
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
