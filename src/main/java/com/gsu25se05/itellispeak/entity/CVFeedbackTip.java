package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "cv_feedback_tip")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVFeedbackTip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "feedback_category_id")
    @JsonBackReference
    private CVFeedbackCategory feedbackCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "type") // good / improve
    private TipType type;

    @Column(name = "tip")
    private String tip;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
}

