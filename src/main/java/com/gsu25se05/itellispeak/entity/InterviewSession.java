package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewSessionId;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(name = "interview_session_thumbnail")
    private String interviewSessionThumbnail;

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

    @Column(nullable = true) // Nullable để thêm tay vào CSDL
    private String source; // Giá trị: SYSTEM, HR, RANDOM

    @OneToMany(mappedBy = "interviewSession", cascade = CascadeType.ALL)
    @JsonIgnore
    @JsonManagedReference
    private List<InterviewHistory> interviewHistories;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = true)
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "company_id", nullable = true)
    @JsonIgnore
    private Company company;

    @ManyToMany
    @JoinTable(
            name = "interview_session_tag",
            joinColumns = @JoinColumn(name = "interview_session_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonManagedReference
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "interview_session_question",
            joinColumns = @JoinColumn(name = "interview_session_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @JsonManagedReference
    private Set<Question> questions = new HashSet<>();

}

