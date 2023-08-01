package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.MeetingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetingLogsRepository extends JpaRepository<MeetingLog, Long> {
    @Query("select log from MeetingLog log " +
            "left join fetch log.attendant a " +
            "left join fetch a.account " +
            "where log.booking.id = ?1 ")
    List<MeetingLog> findAllByBookingId(long bookingId);
}
