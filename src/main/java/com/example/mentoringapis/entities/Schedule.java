package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "available_time")
@Getter
@Setter
public class Schedule {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rrule;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime seedTime ;

    @Temporal(TemporalType.TIME)
    private LocalTime slotTime;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private UserProfile mentor;

}
