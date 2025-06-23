package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.InterviewHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewHistoryDetailRepository extends JpaRepository<InterviewHistoryDetail, Long> {
    List<InterviewHistoryDetail> findByInterviewHistory_InterviewHistoryId(Long interviewHistoryId);
}
