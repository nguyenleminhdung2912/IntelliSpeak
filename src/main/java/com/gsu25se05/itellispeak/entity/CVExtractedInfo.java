package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cv_extracted_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVExtractedInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_cv_id")
    private MemberCV memberCV;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "total_years_experience")
    private Integer totalYearsExperience;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "university")
    private String university;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; // có thể lưu JSON dạng ["Java", "Spring Boot", ...]

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "career_goals", columnDefinition = "TEXT")
    private String careerGoals;

    @Column(name = "work_experience", columnDefinition = "TEXT")
    private String workExperience;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}

