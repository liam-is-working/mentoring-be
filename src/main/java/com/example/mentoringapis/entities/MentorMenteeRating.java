package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Table(name = "mentor_mentee_rating")
@Getter
@Setter
@IdClass(MenteeMentorId.class)
@Entity
public class MentorMenteeRating {
    @Id
    @Column(name = "mentor_id")
    private UUID mentorId;

    @Id
    @Column(name = "mentee_id")
    private UUID menteeId;

    private float rating;
}
