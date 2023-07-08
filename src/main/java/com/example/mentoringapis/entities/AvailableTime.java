package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.fortuna.ical4j.model.Recur;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Entity
@Table(name = "available_time")
@Getter
@Setter
public class AvailableTime {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rrule;

    @Temporal(TemporalType.DATE)
    private LocalDate startDate ;

    @Temporal(TemporalType.DATE)
    private LocalDate endDate ;

    @Temporal(TemporalType.TIME)
    private LocalTime startTime;

    @Temporal(TemporalType.TIME)
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private UserProfile mentor;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<AvailableTimeException> availableTimeExceptionSet = new HashSet<>();

    public Set<LocalDate> exceptionDates(){
        return availableTimeExceptionSet.stream().map(AvailableTimeException::getExceptionDate).collect(Collectors.toSet());
    }

    public LocalDate endDate(){
        return endDate==null?LocalDate.MAX:endDate;
    }

    public Optional<Recur<LocalDateTime>> recurRule(){
        return ofNullable(rrule).map(r -> new Recur<>(rrule));
    }

}
