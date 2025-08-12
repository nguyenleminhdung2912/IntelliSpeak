package com.gsu25se05.itellispeak.dto.interview_session;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.entity.Topic;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewSessionDTO {
    private Long interviewSessionId;
    private String title;
    private String description;
    private String interviewSessionThumbnail;
    private Integer totalQuestion;
    private String difficulty;
    private Set<Long> questionIds;
    private Set<Long> tagIds; // For request
    private Long topicId;     // For request
    private Set<Tag> tags;    // For response
    private Topic topic;      // For response
    private Duration durationEstimate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean isDeleted;
}
