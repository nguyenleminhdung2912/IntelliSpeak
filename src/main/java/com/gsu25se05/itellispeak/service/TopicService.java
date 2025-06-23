package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.interview_topic.TopicRequest;
import com.gsu25se05.itellispeak.entity.Topic;
import com.gsu25se05.itellispeak.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor // Tự động tạo constructor cho các final fields (thay cho @Autowired)
public class TopicService {

    private final TopicRepository topicRepository;

    /**
     * Lấy tất cả các topic chưa bị xóa.
     * @return Danh sách Topic.
     */
    public List<Topic> getAllTopics() {
        return topicRepository.findAllByIsDeletedFalse();
    }

    /**
     * Lấy một topic theo ID.
     * @param id ID của topic.
     * @return Topic tìm thấy.
     * @throws ResourceNotFoundException nếu không tìm thấy topic.
     */
    public Topic getTopicById(Long id) {
        return topicRepository.findByTopicIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
    }

    /**
     * Tạo một topic mới.
     * @param topicRequest Dữ liệu để tạo topic.
     * @return Topic đã được tạo.
     */
    @Transactional
    public Topic createTopic(TopicRequest topicRequest) {
        Topic topic = new Topic();
        topic.setTitle(topicRequest.getTitle());
        topic.setDescription(topicRequest.getDescription());
        topic.setCreateAt(LocalDateTime.now());
        topic.setIsDeleted(false); // Mặc định khi tạo mới
        return topicRepository.save(topic);
    }

    /**
     * Cập nhật một topic đã có.
     * @param id ID của topic cần cập nhật.
     * @param topicRequest Dữ liệu cập nhật.
     * @return Topic đã được cập nhật.
     * @throws ResourceNotFoundException nếu không tìm thấy topic.
     */
    @Transactional
    public Topic updateTopic(Long id, TopicRequest topicRequest) {
        Topic existingTopic = getTopicById(id); // Dùng lại getTopicById để kiểm tra tồn tại và isDeleted
        existingTopic.setTitle(topicRequest.getTitle());
        existingTopic.setDescription(topicRequest.getDescription());
        existingTopic.setUpdateAt(LocalDateTime.now());
        return topicRepository.save(existingTopic);
    }

    /**
     * Xóa mềm một topic.
     * @param id ID của topic cần xóa.
     * @throws ResourceNotFoundException nếu không tìm thấy topic.
     */
    @Transactional
    public void deleteTopic(Long id) {
        Topic topicToDelete = getTopicById(id); // Dùng lại getTopicById để kiểm tra tồn tại và isDeleted
        topicToDelete.setIsDeleted(true);
        topicToDelete.setUpdateAt(LocalDateTime.now());
        topicRepository.save(topicToDelete);
    }
}