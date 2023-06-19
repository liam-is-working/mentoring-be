package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Topic;
import org.springframework.data.repository.CrudRepository;

public interface TopicRepository extends CrudRepository<Topic, Long> {
}
