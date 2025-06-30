package com.gsu25se05.itellispeak.dto.interview_session;

import com.gsu25se05.itellispeak.entity.Tag;
import jakarta.persistence.Column;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSessionDTO {
    private Long interviewSessionId;
    private String title;
    private String description;
    private Integer totalQuestion;
    private String difficulty;
    private Set<Long> questionIds;
    private Duration durationEstimate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean isDeleted;
    private Set<Tag> tags;
}
