package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cv_submission")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CVSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "member_cv_id", nullable = false)
    private MemberCV memberCV;

    @Column(name = "is_viewed")
    private Boolean isViewed = null;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
