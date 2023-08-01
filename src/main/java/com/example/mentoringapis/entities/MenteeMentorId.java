package com.example.mentoringapis.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenteeMentorId implements Serializable {
    private UUID mentorId;
    private UUID menteeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenteeMentorId that = (MenteeMentorId) o;
        return mentorId.equals(that.mentorId) && menteeId.equals(that.menteeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mentorId, menteeId);
    }
}
