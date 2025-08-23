package com.gsu25se05.itellispeak.dto.reply;

import com.gsu25se05.itellispeak.entity.ForumPost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReplyPostResponseDTO {
    private Long replyId;
    private ForumPost forumPost;
    private String content;
    private LocalDateTime updateAt;
}
