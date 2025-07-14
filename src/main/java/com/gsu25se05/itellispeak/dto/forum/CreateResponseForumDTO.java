package com.gsu25se05.itellispeak.dto.forum;

import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateResponseForumDTO {
    private Long postId;

    private String title;

    private String content;

    private List<String> image;

    private ForumTopicType forumTopicType;

//    private ForumCategory forumCategory;

    private LocalDateTime createAt;
}
