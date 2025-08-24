package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Đã import đúng

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository // Thay @Registered bằng @Repository
public interface WebsiteFeedbackRepository extends JpaRepository<WebsiteFeedback, UUID> {

    Optional<WebsiteFeedback> findById(UUID id);
}