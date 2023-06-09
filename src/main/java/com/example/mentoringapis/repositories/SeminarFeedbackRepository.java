package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Seminar;
import com.example.mentoringapis.entities.SeminarFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeminarFeedbackRepository extends JpaRepository<SeminarFeedback, Long> {
    List<SeminarFeedback> findSeminarFeedbackBySeminar(Seminar seminar);
}
