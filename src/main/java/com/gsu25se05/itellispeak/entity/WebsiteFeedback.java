package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    private UUID websiteFeedbackId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expression")
    @Enumerated(EnumType.STRING)
    private expressions expression;

    @Column(name = "description")
    private String description;

    public enum expressions {
        VERY_SATISFIED("Very Satisfied"),
        SATISFIED("Satisfied"),
        NEUTRAL("Neutral"),
        DISSATISFIED("Dissatisfied"),
        VERY_DISSATISFIED("Very Dissatisfied");

        private final String label;

        expressions(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
