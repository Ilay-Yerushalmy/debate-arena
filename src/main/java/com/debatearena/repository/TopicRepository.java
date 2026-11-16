package com.debatearena.repository;

import com.debatearena.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsByTitleIgnoreCase(String title);

    List<Topic> findAllByOrderByIdAsc();
}
