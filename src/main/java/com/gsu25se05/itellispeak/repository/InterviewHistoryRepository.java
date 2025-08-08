package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.InterviewHistory;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewHistoryRepository extends JpaRepository<InterviewHistory, Long> {
    List<InterviewHistory> findByUser_UserId(UUID userId);

    List<InterviewHistory> findByUser(User currentUser);
}
