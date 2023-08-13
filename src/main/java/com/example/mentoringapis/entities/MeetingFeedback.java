package com.example.mentoringapis.entities;

import com.example.mentoringapis.utilities.DateTimeUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "meeting_feedbacks")
@Getter
@Setter
public class MeetingFeedback {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private ZonedDateTime latestDate;
    private int rating;
    private ZonedDateTime createdDate;

    private ZonedDateTime updatedDate;

    @PreUpdate
    protected void onUpdate() {
        updatedDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);
    }

    @PrePersist
    protected void onCreate() {
        createdDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);
        updatedDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giver_id")
    private UserProfile giver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserProfile receiver;

}
