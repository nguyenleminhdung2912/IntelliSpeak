package com.gsu25se05.itellispeak.dto.interview_topic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicRequest {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    private String description;
}
