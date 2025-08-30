package com.gsu25se05.itellispeak.dto.cv;

import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.CVEvaluate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVEvaluateResponseDTO {
    private CVEvaluate evaluate;
    private List<InterviewSessionDTO> recommendSessions;
}
