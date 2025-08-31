package com.gsu25se05.itellispeak.dto.interview_session;

import com.gsu25se05.itellispeak.entity.Difficulty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateInterviewSessionRequestDTO {
    private Long topicId;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Set<Long> tagIds;
}
