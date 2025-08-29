package com.gsu25se05.itellispeak.dto.cv;


import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.CVEvaluate;
import com.gsu25se05.itellispeak.entity.CVExtractedInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CVAnalysisResponseDTO {

    private CVEvaluate evaluation;
    private CVExtractedInfo extractedInfo;
    private List<InterviewSessionDTO> recommendedSessions;
}
