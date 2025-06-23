package com.gsu25se05.itellispeak.dto.forumtopic;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumTopicRequest {
    @NotBlank(message = "Topic title cannot be empty")
    private String title;
}
