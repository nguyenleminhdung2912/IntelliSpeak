package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.interview_topic.TopicRequest;
import com.gsu25se05.itellispeak.entity.Topic;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.repository.TopicRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor // Tự động tạo constructor cho các final fields (thay cho @Autowired)
public class TopicService {

    private final TopicRepository topicRepository;
    private final AccountUtils accountUtils;

    public List<Topic> getAllTopics() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return null;
        }
        return topicRepository.findAllByIsDeletedFalse();
    }

    public Topic getTopicById(Long id) {
        return topicRepository.findByTopicIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
    }

    @Transactional
    public Topic createTopic(TopicRequest topicRequest) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Topic();
        }

        Topic topic = new Topic();
        topic.setTitle(topicRequest.getTitle());
        topic.setDescription(topicRequest.getDescription());
        topic.setCreateAt(LocalDateTime.now());
        topic.setIsDeleted(false); // Mặc định khi tạo mới
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic updateTopic(Long id, TopicRequest topicRequest) {
        Topic existingTopic = getTopicById(id); // Dùng lại getTopicById để kiểm tra tồn tại và isDeleted
        existingTopic.setTitle(topicRequest.getTitle());
        existingTopic.setDescription(topicRequest.getDescription());
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
}