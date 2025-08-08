package com.gsu25se05.itellispeak.dto.interview_session;

import lombok.Data;

import java.util.Set;

@Data
public class QuestionSelectionRequestDTO {
    private int numberOfQuestion;
    private Long topicId;
    private Set<Long> tagIds; // Optional, can be empty
}
