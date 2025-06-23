package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interview_history")
public class InterviewHistory {
    @Id
    @GeneratedValue
    private Long interviewHistoryId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    @Column(nullable = false)
    private Integer totalQuestion;

    @Column
    private Double averageScore;

    @Column(columnDefinition = "TEXT")
    private String aiOverallEvaluate;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "interviewHistory", cascade = CascadeType.ALL)
    private List<InterviewHistoryDetail> details;
}

