package com.debatearena.repository;

import com.debatearena.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByTopicIdOrderByIdAsc(Long topicId);
}
