package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.interview_topic.TopicRequest;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.entity.Topic;
import com.gsu25se05.itellispeak.repository.TagRepository;
import com.gsu25se05.itellispeak.repository.TopicRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor // Tự động tạo constructor cho các final fields (thay cho @Autowired)
public class TopicService {

    private final TopicRepository topicRepository;
    private final AccountUtils accountUtils;
    private final TagRepository tagRepository;


    public List<Topic> getAllTopics() {
//        User user = accountUtils.getCurrentAccount();
//        if (user == null) {
//            return null;
//        }
        return topicRepository.findAllByIsDeletedFalse();
    }

    public Topic getTopicById(Long id) {
        return topicRepository.findByTopicIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
    }

    @Transactional
    public Topic createTopic(TopicRequest topicRequest) {
//        User user = accountUtils.getCurrentAccount();
//        if (user == null) {
//            return new Topic();
//        }

        Topic topic = new Topic();
        topic.setTitle(topicRequest.getTitle());
        topic.setDescription(topicRequest.getDescription());
        topic.setLongDescription(topicRequest.getLongDescription());
        topic.setThumbnail(topicRequest.getThumbnail());
        topic.setCreateAt(LocalDateTime.now());
        topic.setUpdateAt(LocalDateTime.now());
        topic.setIsDeleted(false); // Mặc định khi tạo mới
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic updateTopic(Long id, TopicRequest topicRequest) {
        Topic existingTopic = getTopicById(id); // Dùng lại getTopicById để kiểm tra tồn tại và isDeleted
        existingTopic.setTitle(topicRequest.getTitle());
        existingTopic.setDescription(topicRequest.getDescription());
        existingTopic.setLongDescription(topicRequest.getLongDescription());
        if (topicRequest.getThumbnail() != "" || topicRequest.getThumbnail() != null) {
            existingTopic.setThumbnail(topicRequest.getThumbnail());
        }
        existingTopic.setUpdateAt(LocalDateTime.now());
        return topicRepository.save(existingTopic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        Topic topicToDelete = getTopicById(id); // Dùng lại getTopicById để kiểm tra tồn tại và isDeleted
        topicToDelete.setIsDeleted(true);
        topicToDelete.setUpdateAt(LocalDateTime.now());
        topicRepository.save(topicToDelete);
    }

    public Topic updateTopicThumbnail(Long id, String thumbnailURL) {
        Topic existingTopic = getTopicById(id); // Dùng lại getTopicById để kiểm tra tồn tại và isDeleted
        existingTopic.setThumbnail(thumbnailURL);
        existingTopic.setUpdateAt(LocalDateTime.now());
        return topicRepository.save(existingTopic);
    }

    // src/main/java/com/gsu25se05/itellispeak/service/TopicService.java

    @Transactional
    public Topic addTopicToTag(Long topicId, Long tagId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        topic.getTags().add(tag);
        tag.getTopics().add(topic);
        topicRepository.save(topic);
        // Optionally save tag as well if needed
        return topic;
    }

    @Transactional
    public Topic removeTopicFromTag(Long topicId, Long tagId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        topic.getTags().remove(tag);
        tag.getTopics().remove(topic);
        topicRepository.save(topic);
        // Optionally save tag as well if needed
        return topic;
    }

    public Set<Tag> getTagsOfTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        return topic.getTags();
    }
}