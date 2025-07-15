package com.gsu25se05.itellispeak.dto.question;

import com.gsu25se05.itellispeak.entity.Tag;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class QuestionDTO {
    private Long questionId;
    private String title;
    private String content;
    private String difficulty;
    private String suitableAnswer1;
    private String suitableAnswer2;
    private boolean is_deleted;
    private Set<Tag> tags;
}
