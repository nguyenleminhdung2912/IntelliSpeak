package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.WebsiteFeedback;
import com.gsu25se05.itellispeak.repository.WebsiteFeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WebsiteFeedbackService {

    private final WebsiteFeedbackRepository websiteFeedbackRepository;

    // Sử dụng constructor injection
    public WebsiteFeedbackService(WebsiteFeedbackRepository websiteFeedbackRepository) {
        this.websiteFeedbackRepository = websiteFeedbackRepository;
    }

    /**
     * Lưu một feedback mới.
     *
     * @param feedback đối tượng WebsiteFeedback cần lưu.
     * @return đối tượng WebsiteFeedback đã được lưu.
     */
    @Transactional
    public WebsiteFeedback saveFeedback(WebsiteFeedback feedback) {
        // Bạn có thể thêm logic kiểm tra hoặc xử lý trước khi lưu ở đây
        // Ví dụ: Gán người dùng hiện tại cho feedback nếu cần
        return websiteFeedbackRepository.save(feedback);
    }

    /**
     * Lấy tất cả các feedback.
     *
     * @return danh sách tất cả WebsiteFeedback.
     */
    public List<WebsiteFeedback> getAllFeedback() {
        return websiteFeedbackRepository.findAll();
    }

    /**
     * Lấy feedback theo ID.
     *
     * @param id UUID của feedback.
     * @return Optional chứa WebsiteFeedback nếu tìm thấy, ngược lại là Optional rỗng.
     */
    public Optional<WebsiteFeedback> getFeedbackById(UUID id) {
        return websiteFeedbackRepository.findById(id);
    }

    /**
     * Xóa feedback theo ID.
     *
     * @param id UUID của feedback cần xóa.
     */
    @Transactional
    public void deleteFeedback(UUID id) {
        websiteFeedbackRepository.deleteById(id);
    }

    /**
     * Lấy feedback theo người dùng.
     * (Ví dụ về một phương thức tùy chỉnh bạn có thể thêm vào repository và service)
     *
     * @param user đối tượng User.
     * @return danh sách WebsiteFeedback của người dùng đó.
     */
    // Để sử dụng phương thức này, bạn cần thêm `List<WebsiteFeedback> findByUser(User user);`
    // vào `WebsiteFeedbackRepository`
    /*
    public List<WebsiteFeedback> getFeedbackByUser(User user) {
        return websiteFeedbackRepository.findByUser(user);
    }
    */

    /**
     * Lấy feedback theo loại expression.
     * (Ví dụ về một phương thức tùy chỉnh bạn có thể thêm vào repository và service)
     *
     * @param expression loại expression.
     * @return danh sách WebsiteFeedback có expression tương ứng.
     */
    // Để sử dụng phương thức này, bạn cần thêm
    // `List<WebsiteFeedback> findByExpression(WebsiteFeedback.expressions expression);`
    // vào `WebsiteFeedbackRepository`
    /*
    public List<WebsiteFeedback> getFeedbackByExpression(WebsiteFeedback.expressions expression) {
        return websiteFeedbackRepository.findByExpression(expression);
    }
    */
}