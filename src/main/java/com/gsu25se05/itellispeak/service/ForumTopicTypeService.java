package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.entity.ForumTopicType;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.ForumCategoryRepository;
import com.gsu25se05.itellispeak.repository.ForumTopicTypeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumTopicTypeService {
    private final ForumTopicTypeRepository forumTopicTypeRepository;

    public ForumTopicTypeService(ForumTopicTypeRepository forumTopicTypeRepository) {
        this.forumTopicTypeRepository = forumTopicTypeRepository;
    }

    public List<ForumTopicType> getAllTopicTypes() {
        return forumTopicTypeRepository.findByIsDeletedFalse();
    }

    public ForumTopicType getTopicTypeById(Long id) {
        return forumTopicTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Forum topic type not found with id: " + id));
    }

    public ForumTopicType createTopicType(ForumTopicType topicType) {
        topicType.setCreateAt(LocalDateTime.now());
        topicType.setDeleted(false);
        return forumTopicTypeRepository.save(topicType);
    }

    public ForumTopicType updateTopicType(Long id, ForumTopicType updated) {
        ForumTopicType topicType = getTopicTypeById(id);
        topicType.setTitle(updated.getTitle());
        topicType.setUpdateAt(LocalDateTime.now());
        return forumTopicTypeRepository.save(topicType);
    }

    public void deleteTopicType(Long id) {
        ForumTopicType topicType = getTopicTypeById(id);
        topicType.setDeleted(true);
        topicType.setUpdateAt(LocalDateTime.now());
        forumTopicTypeRepository.save(topicType);
    }
}
