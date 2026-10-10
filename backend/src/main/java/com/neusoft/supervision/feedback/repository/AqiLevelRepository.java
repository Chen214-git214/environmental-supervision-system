package com.neusoft.supervision.feedback.repository;

import com.neusoft.supervision.feedback.entity.AqiLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AqiLevelRepository extends JpaRepository<AqiLevel, Long> {
    Optional<AqiLevel> findByLevelName(String levelName);
}
