package com.gsu25se05.itellispeak.dto.ai_evaluation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewSessionDto {
    private Long interviewSessionId;
    private String title;
    private String description;
    private int totalQuestion;
    private String durationEstimate;
    private List<QuestionDto> questions;
}
