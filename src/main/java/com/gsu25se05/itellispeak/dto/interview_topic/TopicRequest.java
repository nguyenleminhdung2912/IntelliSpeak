package com.gsu25se05.itellispeak.dto.interview_topic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;
}
