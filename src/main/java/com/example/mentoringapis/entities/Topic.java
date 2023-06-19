package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "topics")
public class Topic {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

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
