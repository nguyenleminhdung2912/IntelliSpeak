package com.gsu25se05.itellispeak.dto.forum;

import java.time.LocalDateTime;

public class ForumPostReplyWithUserDTO {
    private Long id;
    private String content;
    private String status;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean isDeleted;
    private UserInfo user;

    public ForumPostReplyWithUserDTO(Long id, String content, String status,
                                     LocalDateTime createAt, LocalDateTime updateAt, Boolean isDeleted, UserInfo user) {
        this.id = id;
        this.content = content;
        this.status = status;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.isDeleted = isDeleted;
        this.user = user;
    }

    public static class UserInfo {
        private String firstName;
        private String lastName;
        private String avatar;

        public UserInfo(String firstName, String lastName, String avatar) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.avatar = avatar;
        }

        // getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateAt() { return createAt; }
    public void setCreateAt(LocalDateTime createAt) { this.createAt = createAt; }
    public LocalDateTime getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDateTime updateAt) { this.updateAt = updateAt; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
}

