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

    @Column(name = "score")
    private Integer score;

    @Column(name = "is_good")
    private Boolean isGood;

    @Column(name = "what_to_improve")
    private String whatToImprove;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}
