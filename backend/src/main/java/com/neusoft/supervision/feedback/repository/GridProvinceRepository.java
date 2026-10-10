package com.neusoft.supervision.feedback.repository;

import com.neusoft.supervision.feedback.entity.GridProvince;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GridProvinceRepository extends JpaRepository<GridProvince, Long> {
    Optional<GridProvince> findByProvinceCode(String provinceCode);
}
