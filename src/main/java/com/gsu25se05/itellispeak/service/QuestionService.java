package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.QuestionStatus;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.repository.TagRepository;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final TagRepository tagRepository;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper, TagRepository tagRepository) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.tagRepository = tagRepository;
    }

    public QuestionDTO save(QuestionDTO dto) {
        Question entity = questionMapper.toEntity(dto);
        entity.setQuestionStatus(QuestionStatus.APPROVED);
        if (dto.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(dto.getTagIds()));
            entity.setTags(tags);
        }
        return questionMapper.toDTO(questionRepository.save(entity));
    }

    public Optional<QuestionDTO> findById(Long id) {
        return questionRepository.findById(id).map(questionMapper::toDTO);
    }

    public List<QuestionDTO> findAll() {
        return questionRepository.findAll().stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
