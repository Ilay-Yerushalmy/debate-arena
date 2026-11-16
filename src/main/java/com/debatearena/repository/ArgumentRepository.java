package com.debatearena.repository;

import com.debatearena.domain.Argument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArgumentRepository extends JpaRepository<Argument, Long> {

    List<Argument> findBySessionIdOrderByIdAsc(Long sessionId);
}
