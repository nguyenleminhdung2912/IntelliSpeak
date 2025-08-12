package com.gsu25se05.itellispeak.dto.ai_evaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationResponseDto {
    private Long questionId;
    private String question;
    private String suitableAnswer1;
    private String suitableAnswer2;
    private String userAnswer;
    private String level;
    private FeedbackDto feedback;
    private String error;
}
