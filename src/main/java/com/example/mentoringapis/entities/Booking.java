package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    public enum Status{
        ACCEPTED, REQUESTED, REJECTED, AVAILABLE, NOT_AVAILABLE
    }

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startTime ;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private UserProfile mentor;

    @ManyToOne
    @JoinColumn(name = "mentee_id")
    private UserProfile mentee;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

}
