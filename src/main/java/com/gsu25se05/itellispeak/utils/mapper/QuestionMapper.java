package com.gsu25se05.itellispeak.utils.mapper;

import com.gsu25se05.itellispeak.dto.interview_session.QuestionInfoDTO;
import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class QuestionMapper {
    public QuestionDTO toDTO(Question entity) {
        if (entity == null) return null;
        return QuestionDTO.builder()
                .questionId(entity.getQuestionId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .difficulty(entity.getDifficulty().name())
                .suitableAnswer1(entity.getSuitableAnswer1())
                .suitableAnswer2(entity.getSuitableAnswer2())
                .isDeleted(entity.getIs_deleted())
                .tags(entity.getTags()) // set tags for output
                .build();
    }

    public Question toEntity(QuestionDTO dto) {
        if (dto == null) return null;
        Question entity = new Question();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setDifficulty(Enum.valueOf(com.gsu25se05.itellispeak.entity.Difficulty.class, dto.getDifficulty()));
        entity.setSuitableAnswer1(dto.getSuitableAnswer1());
        entity.setSuitableAnswer2(dto.getSuitableAnswer2());
        entity.setIs_deleted(false);
        return entity;
    }

    public QuestionInfoDTO toInfoDTO(Question question) {
        if (question == null) return null;
        return QuestionInfoDTO.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .content(question.getContent())
                .suitableAnswer1(question.getSuitableAnswer1())
                .suitableAnswer2(question.getSuitableAnswer2())
                .difficulty(question.getDifficulty().name())
                .tags(question.getTags().stream().map(Tag::getTitle).collect(Collectors.toSet()))
                .build();
    }
}
