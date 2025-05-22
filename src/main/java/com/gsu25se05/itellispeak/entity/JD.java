package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jd")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class JD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jd_id")
    private Long jdId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "link_to_jd")
    private String linkToJd;

    // === Thêm mới ===
    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary; // tóm tắt JD do AI tạo

    @Column(name = "must_have_skills", columnDefinition = "TEXT")
    private String mustHaveSkills;

    @Column(name = "nice_to_have_skills", columnDefinition = "TEXT")
    private String niceToHaveSkills;

    @Column(name = "suitable_level")
    private String suitableLevel; // fresher, junior, mid, etc.

    @Column(name = "recommended_learning", columnDefinition = "TEXT")
    private String recommendedLearning;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}


