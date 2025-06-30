package com.gsu25se05.itellispeak.utils.mapper;

import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.entity.Question;
import org.springframework.stereotype.Component;

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
                .tags(entity.getTags())
                .build();
    }

    public Question toEntity(QuestionDTO dto) {
        if (dto == null) return null;
        Question entity = new Question();
        entity.setQuestionId(dto.getQuestionId());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setDifficulty(Enum.valueOf(com.gsu25se05.itellispeak.entity.Difficulty.class, dto.getDifficulty()));
        entity.setSuitableAnswer1(dto.getSuitableAnswer1());
        entity.setSuitableAnswer2(dto.getSuitableAnswer2());
        entity.setTags(dto.getTags());
        return entity;
    }
}
