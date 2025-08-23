package com.gsu25se05.itellispeak.dto.auth.reponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyInterviewScoreDTO {
    private String date; // e.g. "22/08/2025"
    private String averageScore; // e.g. "7.8"
}
