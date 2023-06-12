package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Table(name = "Seminars")
@NoArgsConstructor
public class Seminar implements Comparable{
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String location;
    private String imageUrl;
    private String attachmentUrl;
    private LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(
            name = "seminars_mentors",
            joinColumns = @JoinColumn(name = "seminar_id"),
            inverseJoinColumns = @JoinColumn(name = "user_profile_id")
    )
    private Set<UserProfile> mentors = new HashSet<>();

    @OneToMany(mappedBy = "seminar", cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY)
    private Set<SeminarFeedback> seminarFeedbacks = new HashSet<>();

    @Override
    public int compareTo(Object o) {
        if(o instanceof Seminar)
        {
            var subject = (Seminar) o;
            var diff1 = Math.abs( ChronoUnit.HOURS.between(LocalDateTime.now(), this.getStartTime()));
            var diff2 = Math.abs( ChronoUnit.HOURS.between(LocalDateTime.now(), subject.getStartTime()));
            var diff = diff1-diff2;
            if(diff==0)
                return 0;
            return diff>0 ? 1 : -1;
        }
        return 0;
    }

    public List<String> mentorNames(){
        return Optional.of(mentors)
                .map(s -> s.stream().map(UserProfile::getFullName).collect(Collectors.toList()))
                .orElse(List.of());
    }
}
