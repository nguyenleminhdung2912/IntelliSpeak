package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jd_evaluate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JDEvaluate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jd_question_id")
    private Long jdQuestionId;

    @ManyToOne
    @JoinColumn(name = "jd_id", nullable = false)
    @JsonIgnore
    private JD jd;

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
