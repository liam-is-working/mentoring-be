package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@IdClass(MenteeMentorId.class)
@Table(name = "mentor_mentee")
public class MentorMentee {
    @Id
    @Column(name = "mentor_id")
    private UUID mentorId;

    @Id
    @Column(name = "mentee_id")
    private UUID menteeId;
}
