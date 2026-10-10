package com.neusoft.supervision.feedback.repository;

import com.neusoft.supervision.feedback.entity.GridCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GridCityRepository extends JpaRepository<GridCity, Long> {
    List<GridCity> findByStatusOrderBySortNoAscCityIdAsc(Integer status);

    List<GridCity> findByProvinceIdAndStatusOrderBySortNoAscCityIdAsc(Long provinceId, Integer status);
}
