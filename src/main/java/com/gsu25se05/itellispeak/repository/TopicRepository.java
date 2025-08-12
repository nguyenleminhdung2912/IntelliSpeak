package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    /**
     * Tìm tất cả các topic chưa bị xóa (isDeleted = false).
     * @return Danh sách các Topic.
     */
    List<Topic> findAllByIsDeletedFalse();

    /**
     * Tìm một topic theo ID và chưa bị xóa.
     * @param topicId ID của topic.
     * @return Optional chứa Topic nếu tìm thấy.
     */
    Optional<Topic> findByTopicIdAndIsDeletedFalse(Long topicId);
}
