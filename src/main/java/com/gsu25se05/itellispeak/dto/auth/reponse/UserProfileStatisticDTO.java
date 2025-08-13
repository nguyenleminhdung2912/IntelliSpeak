package com.gsu25se05.itellispeak.dto.auth.reponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileStatisticDTO {
    private String interviewWeeklyCount;
    private String comparedToLastWeek;
    private String averageInterviewScore;
    private String scoreEvaluate;
    private String answeredQuestionCount;
    private String answeredQuestionComparedToLastWeek;
}
