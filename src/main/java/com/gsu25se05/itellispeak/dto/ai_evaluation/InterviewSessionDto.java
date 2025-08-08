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
public class InterviewSessionDto {
    private int interviewSessionId;
    private String title;
    private String description;
    private int totalQuestion;
    private String durationEstimate;
    private List<QuestionDto> questions;
}
