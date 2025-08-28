package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_jd_evaluate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class CompanyJDEvaluate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_jd_question_id")
    private Long companyJdQuestionId;

    @ManyToOne
    @JoinColumn(name = "company_jd_id", nullable = false)
    @JsonIgnore
    private CompanyJD companyJD;

    @Column(name = "question", columnDefinition = "TEXT")
    private String question;

    @Column(name = "suitable_answer_1", columnDefinition = "TEXT")
    private String suitableAnswer1;

    @Column(name = "suitable_answer_2", columnDefinition = "TEXT")
    private String suitableAnswer2;

    @Column(name = "skill_needed")
    private String skillNeeded;

    @Column(name = "difficulty_level") // dễ / trung bình / khó
    private String difficultyLevel;

    @Column(name = "question_type") // technical / behavior / logic...
    private String questionType;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}
