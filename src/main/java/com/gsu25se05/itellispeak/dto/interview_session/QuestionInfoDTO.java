package com.gsu25se05.itellispeak.dto.interview_session;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class QuestionInfoDTO {
    private Long questionId;
    private String title;
    private String content;
    private String suitableAnswer1;
    private String suitableAnswer2;
    private String difficulty;
    private Set<String> tags; // Tag titles
}
