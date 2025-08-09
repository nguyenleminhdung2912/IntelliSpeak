package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByTopic_TopicIdAndIsDeletedFalse(Long topicId);
    void deleteBySourceAndCreateAtBefore(String source, LocalDateTime threshold);
    List<InterviewSession> findAllBySourceNotOrSourceIsNull(String source);

    List<InterviewSession> findByCreatedBy(User createdBy);


}
