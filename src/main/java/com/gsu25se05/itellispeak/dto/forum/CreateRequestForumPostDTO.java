package com.gsu25se05.itellispeak.dto.forum;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
public class CreateRequestForumPostDTO {
    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @JsonProperty("images")
    private List<String> images;

    @NotNull(message = "TopicTypeId cannot be null")
    @JsonProperty("forumTopicTypeId")
    private Long forumTopicTypeId;

//    private Long forumCategoryId;
}
