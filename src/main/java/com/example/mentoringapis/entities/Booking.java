package com.example.mentoringapis.entities;

import com.example.mentoringapis.utilities.DateTimeUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    public enum Status{
        ACCEPTED, REQUESTED, REJECTED
    }

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    @Temporal(TemporalType.TIME)
    private LocalTime startTime ;

    @Temporal(TemporalType.TIME)
    private LocalTime endTime ;

    @Temporal(TemporalType.DATE)
    private LocalDate bookingDate ;

    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;

    @PreUpdate
    protected void onUpdate() {
        updatedDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);
    }

    @PrePersist
    protected void onCreate() {
        createdDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private UserProfile mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserProfile owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancel_by")
    private UserProfile cancelBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    private String description;
    private String reasonToCancel;
    private boolean didMentorAttend;

    @OneToMany(mappedBy = "booking", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private Set<BookingMentee> bookingMentees = new HashSet<>();

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    private Set<MeetingLog> meetingLogs = new HashSet<>();

    public String startTimeAsString(){
        return startTime.format(DateTimeUtils.DEFAULT_TIME_FORMATTER);
    }

    public String endTimeAsString(){
        return endTime.format(DateTimeUtils.DEFAULT_TIME_FORMATTER);
    }

    public String bookDateAsString(){
        return bookingDate.format(DateTimeUtils.DEFAULT_DATE_FORMATTER);
    }

    public String createDateAsString(){
        return createdDate.withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE).toLocalDateTime().format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER);
    }

    public UserProfile owner(){
        return Objects.requireNonNull(bookingMentees.stream()
                .filter(BookingMentee::isOwner)
                .findFirst()
                .map(BookingMentee::getMentee)
                .orElse(null));
    }

    public List<UserProfile> mentees(){
        return bookingMentees.stream()
                .map(BookingMentee::getMentee)
                .toList();
    }
}
