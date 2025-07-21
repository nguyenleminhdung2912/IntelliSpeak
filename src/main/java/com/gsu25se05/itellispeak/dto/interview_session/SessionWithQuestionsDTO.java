package com.gsu25se05.itellispeak.dto.interview_session;

import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class SessionWithQuestionsDTO {
    private Long interviewSessionId;
    private String title;
    private String description;
    private Integer totalQuestion;
    private Duration durationEstimate;
    private List<QuestionInfoDTO> questions;
}
