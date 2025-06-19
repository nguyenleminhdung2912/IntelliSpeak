package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.category.CategoryResponse;
import com.gsu25se05.itellispeak.dto.topic.TopicRequest;
import com.gsu25se05.itellispeak.dto.topic.TopicResponse;
import com.gsu25se05.itellispeak.dto.topic.UpdateTopicRequest;
import com.gsu25se05.itellispeak.dto.topic.UpdateTopicResponse;
import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.service.CreateServiceException;
import com.gsu25se05.itellispeak.repository.ForumCategoryRepository;
import com.gsu25se05.itellispeak.repository.ForumTopicTypeRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumTopicTypeService {
    private final ForumTopicTypeRepository forumTopicTypeRepository;
    private final AccountUtils accountUtils;


    public ForumTopicTypeService(ForumTopicTypeRepository forumTopicTypeRepository, AccountUtils accountUtils) {
        this.forumTopicTypeRepository = forumTopicTypeRepository;
        this.accountUtils = accountUtils;
    }

    public List<ForumTopicType> getAllTopicTypes() {
        return forumTopicTypeRepository.findByIsDeletedFalse();
    }

    public ForumTopicType getTopicTypeById(Long id) {
        return forumTopicTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Forum topic type not found with id: " + id));
    }

    public Response<TopicResponse> createTopicType(@Valid TopicRequest topicRequest) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);

        ForumTopicType forumTopicType = new ForumTopicType();
        forumTopicType.setTitle(topicRequest.getTitle());
        forumTopicType.setCreateAt(LocalDateTime.now());
        forumTopicType.setDeleted(false);

        try {
            forumTopicTypeRepository.save(forumTopicType);
        } catch (Exception e) {
            throw new CreateServiceException("There was something wrong when creating the topic, please try again...");
        }

        TopicResponse topicResponse = new TopicResponse(
                forumTopicType.getId(),
                forumTopicType.getTitle(),
                forumTopicType.getCreateAt()
        );

        return new Response<>(200, "Category created successfully!", topicResponse);

    }

    public Response<UpdateTopicResponse> updateTopicType(Long id, @Valid UpdateTopicRequest updateTopicRequest) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);

        ForumTopicType topic = forumTopicTypeRepository.findById(id).orElse(null);
        if (topic == null) return new Response<>(404, "Topic not found", null);

        if (updateTopicRequest.getTitle() != null) topic.setTitle(updateTopicRequest.getTitle());
        topic.setUpdateAt(LocalDateTime.now());
        try {
            forumTopicTypeRepository.save(topic);
        } catch (Exception e) {
            throw new CreateServiceException("There was something wrong when updating the topic, please try again...");
        }

        UpdateTopicResponse data = new UpdateTopicResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getUpdateAt()
        );
        return new Response<>(200, "Topic updated successfully!", data);

    }

    public void deleteTopicType(Long id) {
        ForumTopicType topicType = getTopicTypeById(id);
        topicType.setDeleted(true);
        topicType.setUpdateAt(LocalDateTime.now());
        forumTopicTypeRepository.save(topicType);
    }
}
