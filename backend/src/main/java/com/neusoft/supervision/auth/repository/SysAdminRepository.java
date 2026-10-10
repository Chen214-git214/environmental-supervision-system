package com.neusoft.supervision.auth.repository;

import com.neusoft.supervision.auth.entity.SysAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysAdminRepository extends JpaRepository<SysAdmin, Long> {
    Optional<SysAdmin> findByUsername(String username);
}
