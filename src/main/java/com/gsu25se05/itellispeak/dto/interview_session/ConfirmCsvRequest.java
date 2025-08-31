package com.gsu25se05.itellispeak.dto.interview_session;

import com.gsu25se05.itellispeak.dto.question.CSVQuestionDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmCsvRequest {
    private Long interviewSessionId;
    private List<CSVQuestionDTO> questions;
}
