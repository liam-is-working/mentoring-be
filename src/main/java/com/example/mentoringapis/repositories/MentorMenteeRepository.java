package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.MenteeMentorId;
import com.example.mentoringapis.entities.MentorMentee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorMenteeRepository extends JpaRepository<MentorMentee, MenteeMentorId> {
}
