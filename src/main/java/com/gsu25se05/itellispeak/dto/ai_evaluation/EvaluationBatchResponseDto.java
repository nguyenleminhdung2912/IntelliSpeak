package com.gsu25se05.itellispeak.dto.ai_evaluation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsu25se05.itellispeak.dto.topic.TagSimpleDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvaluationBatchResponseDto {
    private Long interviewHistoryId;
    private Long interviewSessionId;
    private String interviewTitle;
    private Integer totalQuestion;
    private Double averageScore;
    private String aiOverallEvaluate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDateTime startedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDateTime endedAt;
    private List<TagSimpleDTO> tags;
    private List<EvaluationResponseDto> results;
}
