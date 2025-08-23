package com.gsu25se05.itellispeak.dto.question;

import com.gsu25se05.itellispeak.entity.Difficulty;
import lombok.Data;

@Data
public class UpdateQuestionDTO {
    private String title;
    private String content;
    private String suitableAnswer1;
    private String suitableAnswer2;
    private Difficulty difficulty;
    private String source;
}
