package com.gsu25se05.itellispeak.dto.forumtopic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumTopicResponse {
    private long topicId;
    private String topicTitle;
    private LocalDateTime createdAt;
}
