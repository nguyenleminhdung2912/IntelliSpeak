package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cv_feedback_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVFeedbackCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cv_evaluate_id")
    @JsonBackReference
    private CVEvaluate cvEvaluate;

    @Column(name = "category_name") // eg: "ATS", "content", etc
    private String categoryName;

    @Column(name = "score")
    private Integer score;

    @OneToMany(mappedBy = "feedbackCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CVFeedbackTip> tips = new ArrayList<>();
}

