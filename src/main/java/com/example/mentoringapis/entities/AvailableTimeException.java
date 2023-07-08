package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "available_time_exceptions")
@Getter
@Setter
public class AvailableTimeException {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private LocalDate exceptionDate ;

    @Temporal(TemporalType.TIME)
    private LocalTime startTime ;

    @Temporal(TemporalType.TIME)
    private LocalTime endTime ;

    private boolean enable;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private AvailableTime parent;

}
