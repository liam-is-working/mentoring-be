package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "seminar_feedbacks")
public class SeminarFeedback {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seminar_id")
    private Seminar seminar;
}
