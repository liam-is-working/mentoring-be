package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Table(name = "mentor_mentee_rating")
@Getter
@Setter
@IdClass(MenteeMentorId.class)
@Entity
public class MentorMenteeRating implements Persistable<MenteeMentorId> {
    @Id
    @Column(name = "mentor_id")
    private UUID mentorId;

    @Id
    @Column(name = "mentee_id")
    private UUID menteeId;

    private Float rating;


    @Override
    public MenteeMentorId getId() {
        return new MenteeMentorId(mentorId, menteeId);
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
