package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interview_session")
public class InterviewSession {
    @Id
    @GeneratedValue
    private Long interviewSessionId;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private Integer totalQuestion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private Duration durationEstimate;

    @Column(nullable = false)
    private LocalDateTime createAt;

    @Column
    private LocalDateTime updateAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "interviewSession", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InterviewHistory> interviewHistories;

    @ManyToMany
    @JoinTable(
            name = "interview_session_tag",
            joinColumns = @JoinColumn(name = "interview_session_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "interview_session_question",
            joinColumns = @JoinColumn(name = "interview_session_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>();
}

