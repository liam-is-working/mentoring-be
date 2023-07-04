package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.entities.UserProfile;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingsRepository extends CrudRepository<Booking, Long> {
    List<Booking> findByMentorEqualsAndStatusLike(UserProfile mentor, String status);
    List<Booking> findByMentorEqualsAndStatusIn(UserProfile mentor, List<String> status);
    List<Booking> findByMentorEqualsAndStartTime(UserProfile mentor, LocalDateTime startTime);
}
