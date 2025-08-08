package com.gsu25se05.itellispeak.dto.ai_evaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDto {

    private int questionId;
    private String title;
    private String content;
    private String suitableAnswer1;
    private String suitableAnswer2;
    private String difficulty;
    private List<String> tags;

}
