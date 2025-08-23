package com.gsu25se05.itellispeak.dto.auth.reponse;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProfileStatisticDTO {
    private String interviewWeeklyCount;
    private String comparedToLastWeek;
    private String averageInterviewScore;
    private String scoreEvaluate;
    private String answeredQuestionCount;
    private String answeredQuestionComparedToLastWeek;
    private List<DailyInterviewScoreDTO> dailyScores; // Add this field
}
