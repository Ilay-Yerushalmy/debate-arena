package com.debatearena.repository;

import com.debatearena.domain.DebateSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DebateSessionRepository extends JpaRepository<DebateSession, Long> {

    List<DebateSession> findAllByOrderByIdDesc();

    List<DebateSession> findByTopicIdOrderByIdDesc(Long topicId);
}
