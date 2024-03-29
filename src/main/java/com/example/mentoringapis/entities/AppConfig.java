package com.example.mentoringapis.entities;

import com.example.mentoringapis.utilities.DateTimeUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "configurations")
@Getter
@Setter
public class AppConfig {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int seminarReportEmailDelay;
    private int invitationEmailDelay;
    private int autoRejectBookingDelay;
    private int maxRequestedBooking;
    private int maxParticipant;
    private int maxCallDuration;
    private int reminderEmailDelay;
    private int maxMentorRecommendation;


    @PrePersist
    protected void onCreate() {
        createdDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);
        setMaxCallDuration(120);
        setReminderEmailDelay(30);
        setMaxMentorRecommendation(10);
        setInvitationEmailDelay(5);
    }

    private ZonedDateTime createdDate;

}
