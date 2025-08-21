package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String suitableAnswer1;

    @Column
    private String suitableAnswer2;

    @Column(columnDefinition = "TEXT")
    private String embeddedVector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStatus questionStatus;

    @Column
    private Boolean is_deleted;

    @Column(nullable = false)
    private String source; //Where is this question from: "geeksforgeeks", "leetcode"

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private QuestionSourceType sourceType;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by", nullable = true)
    private User createdBy;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InterviewHistoryDetail> historyDetails;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @ManyToMany
    @JoinTable(
            name = "question_tag",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonBackReference
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(mappedBy = "questions")
    @JsonBackReference
    private Set<InterviewSession> interviewSessions = new HashSet<>();
}

