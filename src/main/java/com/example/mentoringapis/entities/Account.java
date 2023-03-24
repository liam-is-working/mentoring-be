package com.example.mentoringapis.entities;

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
    public Account(String email, String firebaseUuid, boolean isMentor) {
        this.email = email;
        this.firebaseUuid = firebaseUuid;
        this.isMentor = isMentor;
    }

    @Id
    @GeneratedValue
    private UUID id;

    private String email;
    private String firebaseUuid;
    private boolean isMentor;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL )
    @PrimaryKeyJoinColumn
    private UserProfile userProfile;
}
