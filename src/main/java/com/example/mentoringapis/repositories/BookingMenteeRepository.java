package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.BookingMentee;
import com.example.mentoringapis.entities.BookingMenteeId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingMenteeRepository extends CrudRepository<BookingMentee, BookingMenteeId> {
    @Query("select bm from BookingMentee bm " +
            "left join fetch bm.booking booking " +
            "where bm.menteeId in ?1 and booking.status <> 'REJECTED' ")
    List<BookingMentee> findAllByMenteeIdInAndStatusNotRejected(List<UUID> menteeIds);

    @Query("select bm from BookingMentee bm " +
            "left join fetch bm.booking booking " +
            "where bm.menteeId in ?1 ")
    List<BookingMentee> findAllByMenteeIdIn(List<UUID> menteeIds);

    @Query("select bm from BookingMentee bm " +
            "left join fetch bm.booking booking " +
            "left join fetch booking.bookingMentees bmx " +
            "left join fetch booking.topic t " +
            "left join fetch t.category " +
            "left join fetch t.field " +
            "left join fetch booking.mentor mentor " +
            "left join fetch mentor.account " +
            "left join fetch bmx.mentee mentee " +
            "left join fetch mentee.account " +
            "left join fetch booking.cancelBy cb " +
            "left join fetch cb.account " +
            "where bm.menteeId in ?1 ")
    List<BookingMentee> findAllByMenteeId(UUID menteeId);


    @Modifying
    @Transactional
    @Query("delete from BookingMentee bm " +
            "where bm.bookingId in ?1 and bm.menteeId = ?2")
    void deleteAllByIds(List<Long> bookingId, UUID menteeId);
}
