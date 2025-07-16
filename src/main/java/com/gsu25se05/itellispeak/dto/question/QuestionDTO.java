package com.gsu25se05.itellispeak.dto.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gsu25se05.itellispeak.entity.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDTO {
    private Long questionId;
    private String title;
    private String content;
    private String difficulty;
    private String suitableAnswer1;
    private String suitableAnswer2;
    private boolean isDeleted;
    private Set<Long> tagIds; // for input
    private Set<Tag> tags; // for output
}
