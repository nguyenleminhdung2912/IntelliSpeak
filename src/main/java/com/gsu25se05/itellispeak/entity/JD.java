package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jd")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jd_id")
    private Long jdId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "link_to_jd")
    private String linkToJd;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}

