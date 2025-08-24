package com.gsu25se05.itellispeak.dto.auth.reponse;

import com.gsu25se05.itellispeak.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProfileDTO {
    private String firstName;
    private String lastName;
    private String userName;
    private String bio;
    private User.Role role;
    private String packageName;
    private String phone;
    private String email;
    private String avatar;
    private String website;
    private String github;
    private String linkedin;
    private String facebook;
    private String youtube;
    private List<UserProfileStatisticDTO> statistic;
    private int cvAnalyzeUsed;
    private int jdAnalyzeUsed;
    private int interviewUsed;
}
