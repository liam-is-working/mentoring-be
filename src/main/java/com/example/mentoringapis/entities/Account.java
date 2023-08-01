package com.example.mentoringapis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "accounts")
@NoArgsConstructor
public class Account {
    public enum Role{
        MENTOR, STAFF, STUDENT, ADMIN
    }
    public enum Status{
        ACTIVATED, WAITING, INVALIDATE
    }

    public Account(String email, Role role) {
        this.email = email;
        this.role = role.name();
    }

    public void setStatus(String changeStatus) {
        if(changeStatus==null || (Status.WAITING.name().equals(changeStatus) && Status.WAITING.name().equals(status)))
            return;
        status = changeStatus;
    }

    @Id
    @GeneratedValue
    private UUID id;

    private String email;
    private String role;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private ZonedDateTime createdDate;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "account")
    @PrimaryKeyJoinColumn
    private UserProfile userProfile;

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;


}
