package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Booking;
import com.example.mentoringapis.entities.UserProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends CrudRepository<Booking, Long> {
    @Query("select booking from Booking booking " +
            "left join fetch booking.topic t " +
            "left join fetch t.category " +
            "left join fetch t.field " +
            "left join fetch booking.mentor mentor " +
            "left join fetch mentor.account " +
            "left join fetch booking.bookingMentees bm " +
            "left join fetch bm.mentee mentee " +
            "left join fetch mentee.account " +
            "left join fetch booking.cancelBy cb " +
            "left join fetch cb.account " +
            "where booking.mentor.accountId = ?1")
    List<Booking> findAllByMentorId(UUID mentorId);

    @Query("select booking from Booking booking " +
            "left join fetch booking.topic t " +
            "left join fetch t.category " +
            "left join fetch t.field " +
            "left join fetch booking.mentor mentor " +
            "left join fetch mentor.account " +
            "left join fetch booking.bookingMentees bm " +
            "left join fetch bm.mentee mentee " +
            "left join fetch mentee.account " +
            "left join fetch booking.cancelBy cb " +
            "left join fetch cb.account " +
            "where t.name LIKE %?1%")
    List<Booking> getAll(String topicName);

    @Query("select booking from Booking booking " +
            "left join fetch booking.mentor mentor " +
            "left join fetch booking.bookingMentees bm " +
            "where booking.id = ?1")
    Optional<Booking> findByBookingId(long id);

    @Override
    @Query("select booking from Booking booking " +
            "left join fetch booking.topic t " +
            "left join fetch t.category " +
            "left join fetch t.field " +
            "left join fetch booking.mentor mentor " +
            "left join fetch mentor.account " +
            "left join fetch booking.bookingMentees bm " +
            "left join fetch bm.mentee mentee " +
            "left join fetch mentee.account " +
            "left join fetch booking.cancelBy cb " +
            "left join fetch cb.account " +
            "where booking.id = ?1")
    Optional<Booking> findById(Long bookingId);

    @Query("select booking from Booking booking " +
            "where booking.mentor.accountId = ?1 and booking.bookingDate >= ?2")
    List<Booking> findAllByMentorIdAndBookingDate( UUID mentorId, LocalDate after);

    @Query("select booking from Booking booking " +
            "left join fetch booking.topic t " +
            "left join fetch t.category " +
            "left join fetch t.field " +
            "left join fetch booking.mentor mentor " +
            "left join fetch mentor.account " +
            "left join fetch booking.bookingMentees bm " +
            "left join fetch bm.mentee mentee " +
            "left join fetch mentee.account " +
            "left join fetch booking.cancelBy cb " +
            "left join fetch cb.account " +
            "where booking.mentor.accountId = :mentorId and booking.bookingDate >= :after and booking.bookingDate <= :before")
    List<Booking> findAllByMentorIdAndBookingDate(@Param("mentorId") UUID mentorId,@Param("after") LocalDate after, @Param("before") LocalDate before);

    @Query("select booking from Booking booking " +
            "where booking.mentor.accountId = :mentorId and booking.bookingDate >= :after and booking.bookingDate <= :before")
    List<Booking> findAllByMentorIdAndBookingDateSimplified(@Param("mentorId") UUID mentorId,@Param("after") LocalDate after, @Param("before") LocalDate before);
}
