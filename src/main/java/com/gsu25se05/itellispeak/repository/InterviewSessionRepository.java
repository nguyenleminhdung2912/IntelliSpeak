package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByTopic_TopicIdAndIsDeletedFalse(Long topicId);
    List<InterviewSession> findAllBySourceNotOrSourceIsNullAndIsDeletedFalse(String source);

    List<InterviewSession> findByIsDeletedFalseAndCreatedBy(User createdBy);

    @EntityGraph(attributePaths = {"topic", "tags", "questions"})
    @Query("""
        SELECT i
        FROM InterviewSession i
        WHERE (i.source IS NULL OR i.source <> :excluded)
          AND i.isDeleted = false
        ORDER BY i.createAt DESC
    """)
    List<InterviewSession> findAllVisibleFetchAll(@Param("excluded") String excluded);

}
