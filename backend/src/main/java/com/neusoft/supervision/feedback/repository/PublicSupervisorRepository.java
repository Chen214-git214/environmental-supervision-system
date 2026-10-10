package com.neusoft.supervision.feedback.repository;

import com.neusoft.supervision.feedback.entity.PublicSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicSupervisorRepository extends JpaRepository<PublicSupervisor, Long> {
    Optional<PublicSupervisor> findByUsername(String username);

    Optional<PublicSupervisor> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);
}
