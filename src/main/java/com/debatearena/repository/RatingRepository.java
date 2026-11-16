package com.debatearena.repository;

import com.debatearena.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByArgumentIdAndRaterId(Long argumentId, Long raterId);

    long countByRaterId(Long raterId);
}
