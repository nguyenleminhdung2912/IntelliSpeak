package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cv_evaluate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVEvaluate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cv_evaluate_id")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "member_cv_id", nullable = false)
    private MemberCV memberCV;

    @Column(name = "overall_score")
    private Integer overallScore;

    @OneToMany(mappedBy = "cvEvaluate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CVFeedbackCategory> categories = new ArrayList<>();

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @JsonProperty("imageURL")
    public String getLinkToCv() {
        return memberCV != null ? memberCV.getLinkToCv() : null;
    }
}
