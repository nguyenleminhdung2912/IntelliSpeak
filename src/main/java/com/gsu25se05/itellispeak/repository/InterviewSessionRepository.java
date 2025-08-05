package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByTopic_TopicIdAndIsDeletedFalse(Long topicId);
}
