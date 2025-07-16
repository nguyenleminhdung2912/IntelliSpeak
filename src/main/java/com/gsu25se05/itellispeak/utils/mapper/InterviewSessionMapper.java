package com.gsu25se05.itellispeak.utils.mapper;


import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    public InterviewSession toEntity(InterviewSessionDTO dto, Set<Question> questions, Set<Tag> tags, Topic topic) {
        InterviewSession entity = new InterviewSession();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setTotalQuestion(dto.getTotalQuestion());
        entity.setDifficulty(Enum.valueOf(Difficulty.class, dto.getDifficulty()));
        entity.setQuestions(questions != null ? questions : new HashSet<>());
        entity.setTags(tags != null ? tags : new HashSet<>());
        entity.setTopic(topic);
        entity.setDurationEstimate(dto.getDurationEstimate());
        entity.setCreateAt(LocalDateTime.now());
        entity.setUpdateAt(LocalDateTime.now());
        entity.setIsDeleted(false);
        return entity;
    }
}
