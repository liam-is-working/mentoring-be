package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.MeetingFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingFeedbackRepository extends JpaRepository<MeetingFeedback, Long> {
    @Query("select fb from MeetingFeedback fb " +
            "left join fetch fb.booking booking " +
            "left join fetch booking.topic t " +
            "left join fetch t.category " +
            "left join fetch t.field " +
            "left join fetch booking.mentor mentor " +
            "left join fetch mentor.account " +
            "left join fetch booking.bookingMentees bm " +
            "left join fetch bm.mentee mentee " +
            "left join fetch mentee.account " +
            "left join fetch fb.giver gv " +
            "left join fetch gv.account acc " +
            "left join fetch fb.receiver rec " +
            "left join fetch rec.account " +
            "where gv.accountId = ?1 or rec.accountId = ?1")
    List<MeetingFeedback> findAllByUser(UUID userId);

    @Query("select fb from MeetingFeedback fb " +
            "left join fetch fb.booking booking " +
            "left join fetch booking.topic t " +
            "left join fetch t.category " +
            "left join fetch t.field " +
            "left join fetch booking.mentor mentor " +
            "left join fetch mentor.account " +
            "left join fetch booking.bookingMentees bm " +
            "left join fetch bm.mentee mentee " +
            "left join fetch mentee.account " +
            "left join fetch fb.giver gv " +
            "left join fetch gv.account acc " +
            "left join fetch fb.receiver rec " +
            "left join fetch rec.account " +
            "where booking.id = ?1")
    List<MeetingFeedback> findAllByBooking(Long bookingId);

    @Query(value = "select receiver_id as mentorId, giver_id as menteeId, avg(rating) as avg\n" +
            "from meeting_feedbacks mb\n" +
            "group by (receiver_id, giver_id)", nativeQuery = true)
    List<AverageRating> getAvgRating();

    interface AverageRating{
        UUID getMentorid();
        UUID getMenteeid();
        Float getAvg();
    }
}
