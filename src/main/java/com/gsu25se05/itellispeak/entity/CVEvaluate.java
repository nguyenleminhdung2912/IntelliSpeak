package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cv_evaluate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVEvaluate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cv_evaluate_id")
    private Long cvEvaluateId;

    @ManyToOne
    @JoinColumn(name = "member_cv_id", nullable = false)
    private MemberCV memberCV;

    // Tổng điểm đánh giá CV (trên thang 10 hoặc 100)
    @Column(name = "score")
    private Integer score;

    // Đánh giá chung: True nếu ổn để apply, False nếu cần cải thiện
    @Column(name = "is_good")
    private Boolean isGood;

    // Góp ý cần cải thiện
    @Column(name = "what_to_improve", columnDefinition = "TEXT")
    private String whatToImprove;

    // Nhận xét về trình bày
    @Column(name = "presentation_feedback", columnDefinition = "TEXT")
    private String presentationFeedback;

    // Nhận xét về lỗi chính tả/ngữ pháp
    @Column(name = "grammar_feedback", columnDefinition = "TEXT")
    private String grammarFeedback;

    // Phân tích sự phù hợp với JD (nếu có JD upload kèm)
    @Column(name = "relevance_score") // eg. 85/100
    private Integer relevanceScore;

    // Các kỹ năng còn thiếu (dưới dạng chuỗi phân cách hoặc bảng riêng nếu cần normalize)
    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    // Các kỹ năng nổi bật được phát hiện trong CV
    @Column(name = "highlighted_skills", columnDefinition = "TEXT")
    private String highlightedSkills;

    // Gợi ý hành động tiếp theo (nên thêm gì, sửa gì, sắp xếp lại...)
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}
