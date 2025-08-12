package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "interview_history_detail")
public class InterviewHistoryDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewHistoryDetailId;

    @ManyToOne
    @JoinColumn(name = "interview_history_id", nullable = false)
    private InterviewHistory interviewHistory;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String answeredContent;

    @Column(nullable = false)
    private Double score;

    @Column(columnDefinition = "TEXT")
    private String aiEvaluatedContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(columnDefinition = "TEXT")
    private String suitableAnswer1;

    @Column(columnDefinition = "TEXT")
    private String suitableAnswer2;
}

