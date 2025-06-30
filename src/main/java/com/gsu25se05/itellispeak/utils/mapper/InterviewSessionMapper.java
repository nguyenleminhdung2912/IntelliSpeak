package com.gsu25se05.itellispeak.utils.mapper;


import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.Question;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InterviewSessionMapper {
    public InterviewSessionDTO toDTO(InterviewSession entity) {
        InterviewSessionDTO dto = new InterviewSessionDTO();
        dto.setInterviewSessionId(entity.getInterviewSessionId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setTotalQuestion(entity.getTotalQuestion());
        dto.setDifficulty(entity.getDifficulty().name());
        dto.setQuestionIds(entity.getQuestions().stream().map(Question::getQuestionId).collect(Collectors.toSet()));
        return dto;
    }

    public InterviewSession toEntity(InterviewSessionDTO dto, Set<Question> questions) {
        InterviewSession entity = new InterviewSession();
        entity.setInterviewSessionId(dto.getInterviewSessionId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setTotalQuestion(dto.getTotalQuestion());
        entity.setDifficulty(Enum.valueOf(com.gsu25se05.itellispeak.entity.Difficulty.class, dto.getDifficulty()));
        entity.setQuestions(questions != null ? questions : new HashSet<>());
        return entity;
    }
}
