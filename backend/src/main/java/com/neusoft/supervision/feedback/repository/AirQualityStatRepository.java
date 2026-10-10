package com.neusoft.supervision.feedback.repository;

import com.neusoft.supervision.feedback.entity.AirQualityStat;
import com.neusoft.supervision.statistics.dto.AirQualityStatResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AirQualityStatRepository extends JpaRepository<AirQualityStat, Long> {

    List<AirQualityStat> findByFeedbackIdOrderByMonitorTimeDesc(Long feedbackId);

    /**
     * 监测统计查询，按城市 / 网格员 / 时间范围筛选，全部条件可选。
     *
     * <p>同时连接城市、网格员与级别三张表，直接取出展示所需的名称，避免逐条回查。</p>
     */
    @Query("""
            SELECT new com.neusoft.supervision.statistics.dto.AirQualityStatResponse(
                stat.statId,
                stat.feedbackId,
                stat.cityId,
                city.cityName,
                stat.gridMemberId,
                member.realName,
                stat.levelId,
                level.levelName,
                stat.aqiValue,
                stat.pm25,
                stat.pm10,
                stat.so2,
                stat.no2,
                stat.co,
                stat.o3,
                stat.monitorTime)
            FROM AirQualityStat stat
            LEFT JOIN GridCity city ON city.cityId = stat.cityId
            LEFT JOIN GridMember member ON member.gridMemberId = stat.gridMemberId
            LEFT JOIN AqiLevel level ON level.levelId = stat.levelId
            WHERE (:cityId IS NULL OR stat.cityId = :cityId)
              AND (:gridMemberId IS NULL OR stat.gridMemberId = :gridMemberId)
              AND (:startTime IS NULL OR stat.monitorTime >= :startTime)
              AND (:endTime IS NULL OR stat.monitorTime <= :endTime)
            ORDER BY stat.monitorTime DESC, stat.statId DESC
            """)
    List<AirQualityStatResponse> search(
            @Param("cityId") Long cityId,
            @Param("gridMemberId") Long gridMemberId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
