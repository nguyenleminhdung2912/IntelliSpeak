package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.dto.question.TagDTO;
import com.gsu25se05.itellispeak.dto.question.TagWithQuestionsDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.entity.Topic;
import com.gsu25se05.itellispeak.repository.QuestionRepository;
import com.gsu25se05.itellispeak.repository.TagRepository;
import com.gsu25se05.itellispeak.repository.TopicRepository;
import com.gsu25se05.itellispeak.utils.mapper.QuestionMapper;
import com.gsu25se05.itellispeak.utils.mapper.TagMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;


    public TagService(TagRepository tagRepository, TagMapper tagMapper, QuestionMapper questionMapper, QuestionRepository questionRepository, TopicRepository topicRepository) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
        this.questionMapper = questionMapper;
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
    }

    public TagDTO save(TagDTO dto) {
        Tag tag = tagMapper.toEntity(dto);
        return tagMapper.toDTO(tagRepository.save(tag));
    }

    public Optional<TagDTO> findById(Long id) {
        return tagRepository.findById(id).map(tagMapper::toDTO);
    }

    public List<TagDTO> findAll() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TagDTO> update(Long id, TagDTO dto) {
        return tagRepository.findById(id).map(existing -> {
            existing.setTitle(dto.getTitle());
            existing.setDescription(dto.getDescription());
            existing.setUpdateAt(dto.getUpdateAt());
            existing.setIsDeleted(dto.getIsDeleted());
            return tagMapper.toDTO(tagRepository.save(existing));
        });
    }

    public boolean delete(Long id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Question addTagToQuestion(Long questionId, Long tagId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        question.getTags().add(tag);
        return questionRepository.save(question);
    }

    @Transactional
    public Question removeTagFromQuestion(Long questionId, Long tagId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        question.getTags().remove(tag);
        return questionRepository.save(question);
    }

    @Transactional
    public List<Question> addTagToQuestions(Long tagId, List<Long> questionIds) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        List<Question> questions = questionRepository.findAllById(questionIds);
        questions.forEach(question ->  question.getTags().add(tag));
        return questionRepository.saveAll(questions);
    }

    public List<TagWithQuestionsDTO> getAllTagsWithQuestions() {
        return tagRepository.findAll().stream()
                .map(tag -> {
                    List<QuestionDTO> questions = tag.getQuestions().stream()
                            .map(questionMapper::toDTO)
                            .collect(Collectors.toList());
                    return tagMapper.toTagWithQuestionsDTO(tag, questions);
                })
                .collect(Collectors.toList());
    }

    // src/main/java/com/gsu25se05/itellispeak/service/TagService.java

    @Transactional
    public Tag addTagToTopic(Long tagId, Long topicId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        tag.getTopics().add(topic);
        topic.getTags().add(tag);
        tagRepository.save(tag);
        // Optionally save topic as well if needed
        return tag;
    }

    @Transactional
    public Tag removeTagFromTopic(Long tagId, Long topicId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        tag.getTopics().remove(topic);
        topic.getTags().remove(tag);
        tagRepository.save(tag);
        // Optionally save topic as well if needed
        return tag;
    }

    public Set<Tag> getTagsByTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        return topic.getTags();
    }

    public Set<Topic> getTopicsByTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        return tag.getTopics();
    }
}