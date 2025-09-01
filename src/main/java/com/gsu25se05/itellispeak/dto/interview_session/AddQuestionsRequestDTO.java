package com.gsu25se05.itellispeak.dto.interview_session;

import lombok.Data;
import java.util.Set;

@Data
public class AddQuestionsRequestDTO {
    private Set<Long> questionIds;
}