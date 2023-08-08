package com.example.mentoringapis.entities;

import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "topic_fields")
public class TopicField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "field", fetch = FetchType.LAZY)
    private Set<Topic> topics;

    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;

    @PreUpdate
    protected void onUpdate() {
        updatedDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);
    }

    @PrePersist
    protected void onCreate() {
        createdDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);
        updatedDate = ZonedDateTime.now().withZoneSameInstant(DateTimeUtils.VIET_NAM_ZONE);     }
}
