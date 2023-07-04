package com.example.mentoringapis.repositories;

import com.example.mentoringapis.entities.Topic;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TopicRepository extends CrudRepository<Topic, Long> {
    @NotNull
    @Override
    @Query("select topic from Topic topic " +
            "left join fetch topic.category " +
            "left join fetch  topic.field " +
            "left join fetch topic.mentor m " +
            "left join fetch m.account")
    public List<Topic> findAll();

    @Query("select topic from Topic topic " +
            "left join fetch topic.category " +
            "left join fetch  topic.field " +
            "left join fetch topic.mentor m " +
            "left join fetch m.account " +
            "where topic.id in ?1")
    public List<Topic> findAllByIdIn(Iterable<Long> ids);
}
