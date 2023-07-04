package com.example.mentoringapis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Departments")
public class Department {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "department",
            fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Account> staffAccounts = new HashSet<>();

    @OneToMany(mappedBy = "department")
    private Set<Seminar> seminars = new HashSet<>();

}
