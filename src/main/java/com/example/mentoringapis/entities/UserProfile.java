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
@NoArgsConstructor
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private UUID accountId;

    private String fullName;
    private String description;
    private String dob;
    private String gender;
    private String avatarUrl;
    private String coverUrl;

    @OneToOne(cascade = CascadeType.MERGE)
    @MapsId
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;
}
