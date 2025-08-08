package com.gsu25se05.itellispeak.dto.ai_evaluation;

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
public class EvaluationBatchResponseDto {
    private Long interviewHistoryId;
    private Long interviewSessionId;
    private Integer totalQuestion;
    private Double averageScore;
    private String aiOverallEvaluate;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<EvaluationResponseDto> results;
}
