package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "booking_mentee")
@IdClass(BookingMenteeId.class)
@NoArgsConstructor
public class BookingMentee {
    @Id
    @Column(name = "booking_id")
    private long bookingId;

    @Id
    @Column(name = "mentee_id")
    private UUID menteeId;

    private boolean isOwner;
    private boolean didMenteeAttend;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("menteeId")
    @JoinColumn(name = "mentee_id")
    private UserProfile mentee;
}
