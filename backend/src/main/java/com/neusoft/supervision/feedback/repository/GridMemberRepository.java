package com.neusoft.supervision.feedback.repository;

import com.neusoft.supervision.feedback.entity.GridMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GridMemberRepository extends JpaRepository<GridMember, Long> {
    Optional<GridMember> findByEmployeeNoAndStatus(String employeeNo, Integer status);

    List<GridMember> findByCityIdAndStatusOrderByRealNameAsc(Long cityId, Integer status);

    List<GridMember> findByProvinceIdNotAndStatusOrderByProvinceNameAscCityNameAscRealNameAsc(Long provinceId, Integer status);

    boolean existsByCityIdAndStatus(Long cityId, Integer status);
}
