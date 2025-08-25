package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Thay @Registered bằng @Repository
public interface WebsiteFeedbackRepository extends JpaRepository<WebsiteFeedback, Long> {

    Optional<WebsiteFeedback> findById(Long id);
}