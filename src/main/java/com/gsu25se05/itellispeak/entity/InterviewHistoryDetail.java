package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_history_detail")
public class InterviewHistoryDetail {
    @Id
    @GeneratedValue
    private Long interviewHistoryDetailId;

    @ManyToOne
    @JoinColumn(name = "interview_history_id", nullable = false)
    private InterviewHistory interviewHistory;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String answeredContent;

    @Column
    private Double score;

    @Column(columnDefinition = "TEXT")
    private String aiEvaluatedContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;
}

