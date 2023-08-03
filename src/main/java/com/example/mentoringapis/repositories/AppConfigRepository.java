package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, Integer> {
}
