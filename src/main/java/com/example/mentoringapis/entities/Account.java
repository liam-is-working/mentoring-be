package com.example.mentoringapis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Accounts")
@NoArgsConstructor
public class Account {
    public enum Role{
        MENTOR, STAFF, STUDENT
    }
    public enum Status{
        NOT_DEFINE, ACTIVATED, WAITING, INVALIDATE
    }
    public Account(String email, String firebaseUuid, Role role) {
        this.email = email;
        this.firebaseUuid = firebaseUuid;
        this.role = role.name();
    }

    public Account(String email, String firebaseUuid, Role role, boolean isAuthenticated) {
        this.email = email;
        this.firebaseUuid = firebaseUuid;
        this.role = role.name();
        this.isAuthenticated = isAuthenticated;
    }

    @Id
    @GeneratedValue
    private UUID id;

    private String email;
    private String firebaseUuid;
    private String role;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private ZonedDateTime createdDate;
    private boolean isAuthenticated;

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
