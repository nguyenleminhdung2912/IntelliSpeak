package com.gsu25se05.itellispeak.dto.forum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostReplyWithUserDTO {
    private Long id;
    private String content;
    private String status;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean isDeleted;
    private UserInfo user;
    private boolean isYours;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String firstName;
        private String lastName;
        private String avatar;
    }
}