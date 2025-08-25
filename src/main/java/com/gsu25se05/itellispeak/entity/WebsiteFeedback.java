package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "website_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebsiteFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "website_feedback_id")
    private Long websiteFeedbackId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "description")
    private String description;

    @Column(name = "isHandled", nullable = true)
    private Boolean isHandled;

    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

}
