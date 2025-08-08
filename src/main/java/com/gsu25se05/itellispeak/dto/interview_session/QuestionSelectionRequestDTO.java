package com.gsu25se05.itellispeak.dto.interview_session;

import lombok.Data;

import java.util.Set;

@Data
public class QuestionSelectionRequestDTO {
    private Long interviewSessionId;
    private int numberOfQuestion;
    private Set<Long> tagIds; // Optional, can be empty
}
