package com.gsu25se05.itellispeak.dto.forum;

import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestForumPostDTO {
    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    private String image;

    private Long forumTopicTypeId;

    private Long forumCategoryId;
}
