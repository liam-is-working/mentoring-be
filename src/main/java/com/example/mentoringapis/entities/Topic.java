package com.example.mentoringapis.entities;

import com.example.mentoringapis.utilities.DateTimeUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Table(name = "topics")
public class Topic {
    public enum Status{
        WAITING, DELETED, ACCEPTED, REJECTED, ARCHIVED
    }
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String status;
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

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private TopicCategory category;

    @ManyToOne
    @JoinColumn(name = "field_id", nullable = false)
    private TopicField field;

    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private UserProfile mentor;

}
