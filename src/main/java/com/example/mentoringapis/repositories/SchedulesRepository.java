package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Schedule;
import com.example.mentoringapis.entities.UserProfile;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalTime;
import java.util.List;

public interface SchedulesRepository extends CrudRepository<Schedule, Long> {
    public List<Schedule> findByMentorEquals(UserProfile mentor);
    public List<Schedule> findByMentorEqualsAndSlotTimeIs(UserProfile mentor, LocalTime slotTime);
}
