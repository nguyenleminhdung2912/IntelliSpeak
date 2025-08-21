package com.gsu25se05.itellispeak.dto.interview_session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionWithQuestionsDTO {
    private Long interviewSessionId;
    private String title;
    private String description;
    private Integer totalQuestion;
    private Duration durationEstimate;
    private Long companyId;
    private List<QuestionInfoDTO> questions;
}
