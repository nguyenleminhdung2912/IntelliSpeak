package com.gsu25se05.itellispeak.dto.jd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {
    private String jdText;
    private String question;
    private String answer;
}
