package com.gsu25se05.itellispeak.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTopicResponse {
    private long topicId;
    private String title;
    private LocalDateTime updatedAt;
}
