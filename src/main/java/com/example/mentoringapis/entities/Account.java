package com.example.mentoringapis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Accounts")
@NoArgsConstructor
public class Account {
    public static enum Role{
        MENTOR, STAFF, STUDENT
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
    private boolean isAuthenticated;

    @JsonIgnore
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL )
    @PrimaryKeyJoinColumn
    private UserProfile userProfile;
}
