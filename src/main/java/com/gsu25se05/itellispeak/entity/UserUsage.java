package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_usage_id")
    private Long userUsageId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "cv_analyze_used", nullable = false)
    private int cvAnalyzeUsed = 0;

    @Column(name = "jd_analyze_used", nullable = false)
    private int jdAnalyzeUsed = 0;

    @Column(name = "interview_used", nullable = false)
    private int interviewUsed = 0;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}
