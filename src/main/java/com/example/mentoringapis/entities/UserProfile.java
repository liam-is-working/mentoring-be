package com.example.mentoringapis.entities;

import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.sql.SQLType;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private UUID accountId;

    private String fullName;
    private String description;
    @Basic
    private Date dob;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String avatarUrl;
    private String phoneNum;
    private String coverUrl;
    @JdbcTypeCode(SqlTypes.JSON)
    private String cv;
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

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;

    @OneToMany(mappedBy = "mentor")
    private Set<AvailableTime> availableTimes = new HashSet<>();

    @OneToMany(mappedBy = "mentor", fetch = FetchType.LAZY)
    private Set<Topic> topics = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "booking_mentee",
            joinColumns = @JoinColumn(name = "mentee_id"),
            inverseJoinColumns = @JoinColumn(name = "booking_id")
    )
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "mentee", fetch = FetchType.LAZY)
    private Set<BookingMentee> bookingMentees = new HashSet<>();

    @ManyToMany(mappedBy = "mentors")
    private Set<Seminar> seminars = new HashSet<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
    private Set<MeetingFeedback> feedbacks = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="mentor_mentee",
            joinColumns=@JoinColumn(name="mentor_id"),
            inverseJoinColumns=@JoinColumn(name="mentee_id")
    )
    private Set<UserProfile> followers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="mentor_mentee",
            joinColumns=@JoinColumn(name="mentee_id"),
            inverseJoinColumns=@JoinColumn(name="mentor_id")
    )
    private Set<UserProfile> followings = new HashSet<>();

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
