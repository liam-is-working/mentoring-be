package com.example.mentoringapis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.sql.SQLType;
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
    @Basic
    private Date dob;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String avatarUrl;
    private String coverUrl;
    @JdbcTypeCode(SqlTypes.JSON)
    private String cv;

    @OneToOne(cascade = CascadeType.MERGE)
    @MapsId
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;
}
