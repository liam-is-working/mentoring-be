package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.AvailableTime;
import com.example.mentoringapis.entities.UserProfile;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalTime;
import java.util.List;

public interface SchedulesRepository extends CrudRepository<AvailableTime, Long> {
    public List<AvailableTime> findByMentorEquals(UserProfile mentor);
//    public List<AvailableTime> findByMentorEqualsAndSlotTimeIs(UserProfile mentor, LocalTime slotTime);
}
