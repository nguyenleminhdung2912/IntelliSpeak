package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Đã import đúng

import java.util.List;
import java.util.UUID;

@Repository // Thay @Registered bằng @Repository
public interface WebsiteFeedbackRepository extends JpaRepository<WebsiteFeedback, UUID> {
    // Ví dụ về các phương thức truy vấn tùy chỉnh:
    /**
     * Tìm tất cả feedback của một người dùng cụ thể.
     * @param user Người dùng
     * @return Danh sách feedback của người dùng đó
     */
    List<WebsiteFeedback> findByUser(User user);

    /**
     * Tìm tất cả feedback theo một loại biểu cảm cụ thể.
     * @param expression Loại biểu cảm
     * @return Danh sách feedback có biểu cảm tương ứng
     */
    List<WebsiteFeedback> findByExpression(WebsiteFeedback.expressions expression);
}