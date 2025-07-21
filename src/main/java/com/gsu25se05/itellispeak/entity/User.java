package com.gsu25se05.itellispeak.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne
    @JoinColumn(name = "package_id", referencedColumnName = "package_id")
    @JsonIgnore
    private Package aPackage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Column
    private LocalDate birthday;

    @Column(length = 255)
    private String avatar;

    @Column(length = 20)
    private String status;

    @Column(length = 1000)
    private String bio;

    @Column(name = "website")
    private String website;

    @Column(name = "github")
    private String github;

    @Column(name = "linkedin")
    private String linkedin;

    @Column(name = "facebook")
    private String facebook;

    @Column(name = "youtube")
    private String youtube;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public User(String email) {
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(this.role.name()));
        return authorities;
    }

    @JsonIgnore
    @Transient
    @Override
    public String getUsername() {
        return this.email;
    }

    @JsonIgnore
    @Transient
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @JsonIgnore
    @Transient
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @JsonIgnore
    @Transient
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @JsonIgnore
    @Transient
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Transient
    private String tokens;

    @Transient
    private String refreshToken;

    @OneToMany(mappedBy = "user")
    private List<MemberCV> cvs;

    @OneToMany(mappedBy = "user")
    private List<JD> jobDescriptions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InterviewHistory> interviewHistories;

    @OneToOne(mappedBy = "user")
    private HR hr;


//    public User(UUID userId, String firstName, String lastName, String email, String password, Role role, String paymentPlan, LocalDate birthday, String avatar, String status, LocalDateTime createAt, LocalDateTime updateAt, Boolean isDeleted, String tokens, String refreshToken) {
//        this.userId = userId;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.email = email;
//        this.password = password;
//        this.role = role;
//        this.paymentPlan = paymentPlan;
//        this.birthday = birthday;
//        this.avatar = avatar;
//        this.status = status;
//        this.createAt = createAt;
//        this.updateAt = updateAt;
//        this.isDeleted = isDeleted;
//        this.tokens = tokens;
//        this.refreshToken = refreshToken;
//    }

    public enum Role {
        USER, HR, ADMIN
    }
}
