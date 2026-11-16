package com.debatearena.repository;

import com.debatearena.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Participant> findAllByOrderByNameAsc();
}
