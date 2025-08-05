package com.gsu25se05.itellispeak.dto.interview_session;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewByTopicDTO {
    private String title;
    private String description;
    private String longDescription;
    private List<InterviewSessionDTO> interviewSessionDTOs;
}
