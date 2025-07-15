package com.gsu25se05.itellispeak.utils.mapper;

import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.dto.question.TagDTO;
import com.gsu25se05.itellispeak.dto.question.TagWithQuestionsDTO;
import com.gsu25se05.itellispeak.entity.Tag;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class TagMapper {
    public TagDTO toDTO(Tag tag) {
        if (tag == null) return null;
        return TagDTO.builder()
                .id(tag.getTagId())
                .title(tag.getTitle())
                .description(tag.getDescription())
                .createAt(tag.getCreateAt())
                .updateAt(tag.getUpdateAt())
                .isDeleted(tag.getIsDeleted())
                .build();
    }

    public Tag toEntity(TagDTO dto) {
        if (dto == null) return null;
        Tag tag = new Tag();
        tag.setTagId(dto.getId());
        tag.setTitle(dto.getTitle());
        tag.setDescription(dto.getDescription());
        tag.setCreateAt(dto.getCreateAt());
        tag.setUpdateAt(dto.getUpdateAt());
        tag.setIsDeleted(dto.getIsDeleted());
        return tag;
    }

    public TagWithQuestionsDTO toTagWithQuestionsDTO(Tag tag, List<QuestionDTO> questions) {
        return TagWithQuestionsDTO.builder()
                .id(tag.getTagId())
                .title(tag.getTitle())
                .description(tag.getDescription())
                .isDeleted(tag.getIsDeleted())
                .questions(questions)
                .build();
    }
}
