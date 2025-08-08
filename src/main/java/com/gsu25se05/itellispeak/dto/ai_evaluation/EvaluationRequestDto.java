package com.gsu25se05.itellispeak.dto.ai_evaluation;

import com.gsu25se05.itellispeak.dto.interview_session.SessionWithQuestionsDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationRequestDto {

    private InterviewSessionDto interviewSession;
    private List<ChatMessageDto> chatHistory;

}
