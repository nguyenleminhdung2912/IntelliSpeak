package com.gsu25se05.itellispeak.utils.mapper;


import com.gsu25se05.itellispeak.dto.ai_evaluation.EvaluationBatchResponseDto;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.dto.topic.TagSimpleDTO;
import com.gsu25se05.itellispeak.entity.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        dto.setInterviewSessionThumbnail(entity.getInterviewSessionThumbnail());
        dto.setDifficulty(entity.getDifficulty().name());
        dto.setQuestionIds(entity.getQuestions().stream().map(Question::getQuestionId).collect(Collectors.toSet()));
        return dto;
    }

    public InterviewSession toEntityWithCompany(InterviewSessionDTO dto, Set<Question> questions, Set<Tag> tags, Topic topic, Company company) {
        InterviewSession entity = new InterviewSession();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setInterviewSessionThumbnail(dto.getInterviewSessionThumbnail());
        entity.setTotalQuestion(dto.getTotalQuestion());
        entity.setDifficulty(Enum.valueOf(Difficulty.class, dto.getDifficulty()));
        entity.setQuestions(questions != null ? questions : new HashSet<>());
        entity.setTags(tags != null ? tags : new HashSet<>());
        entity.setTopic(topic);
        entity.setDurationEstimate(Duration.ofMinutes(10));
        entity.setCreateAt(LocalDateTime.now());
        entity.setUpdateAt(LocalDateTime.now());
        entity.setCompany(company);
        entity.setIsDeleted(false);
        return entity;
    }
    public InterviewSession toEntity(InterviewSessionDTO dto, Set<Question> questions, Set<Tag> tags, Topic topic) {
        InterviewSession entity = new InterviewSession();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setInterviewSessionThumbnail(dto.getInterviewSessionThumbnail());
        entity.setTotalQuestion(dto.getTotalQuestion());
        entity.setDifficulty(Enum.valueOf(Difficulty.class, dto.getDifficulty()));
        entity.setQuestions(questions != null ? questions : new HashSet<>());
        entity.setTags(tags != null ? tags : new HashSet<>());
        entity.setTopic(topic);
        entity.setDurationEstimate(Duration.ofMinutes(10));
        entity.setCreateAt(LocalDateTime.now());
        entity.setUpdateAt(LocalDateTime.now());
        entity.setIsDeleted(false);
        return entity;
    }

    public EvaluationBatchResponseDto toEvaluationBatchResponseForGetAllDto(InterviewSession interviewSession) {
        EvaluationBatchResponseDto responseDto = new EvaluationBatchResponseDto();
        responseDto.setInterviewHistoryId(null);
        responseDto.setInterviewSessionId(interviewSession.getInterviewSessionId());
        responseDto.setInterviewTitle(interviewSession.getTitle());
        responseDto.setTotalQuestion(interviewSession.getTotalQuestion());
        List<TagSimpleDTO> tagSimpleDTOS = new ArrayList<>();
        for (Tag tag : interviewSession.getTags()) {
            TagSimpleDTO tagSimpleDTO = new TagSimpleDTO();
            tagSimpleDTO.setTitle(tag.getTitle());
            tagSimpleDTO.setTagId(tag.getTagId());
            tagSimpleDTOS.add(tagSimpleDTO);
        }
        responseDto.setTags(tagSimpleDTOS);
        responseDto.setAverageScore(null);
        responseDto.setAiOverallEvaluate(null);
        responseDto.setStartedAt(null);
        responseDto.setEndedAt(null);

        return responseDto;
    }
}
