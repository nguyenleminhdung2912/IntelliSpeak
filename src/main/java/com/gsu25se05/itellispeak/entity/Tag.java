package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tag")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private LocalDateTime createAt;

    @Column
    private LocalDateTime updateAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    // many-to-many with question
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Question> questions;

    // many-to-many with interview session
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<InterviewSession> interviewSessions;
}

