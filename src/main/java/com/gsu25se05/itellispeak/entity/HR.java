package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hr")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hr_id")
    private Long hrId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id")
    private Company company;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "linkedin_url", columnDefinition = "TEXT")
    private String linkedinUrl;

    @Column(name = "cv_url", columnDefinition = "TEXT")
    private String cvUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private HRStatus status; // e.g., "PENDING", "APPROVED", "REJECTED"

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
