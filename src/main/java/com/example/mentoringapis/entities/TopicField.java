package com.example.mentoringapis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "topic_fields")
public class TopicField {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
}
