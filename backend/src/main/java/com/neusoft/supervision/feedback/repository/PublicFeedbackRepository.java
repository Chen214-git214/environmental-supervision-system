package com.neusoft.supervision.feedback.repository;

import com.neusoft.supervision.feedback.entity.PublicFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicFeedbackRepository extends JpaRepository<PublicFeedback, Long> {
    List<PublicFeedback> findByAssignedGridMemberIdOrderByAssignedAtDescSubmittedAtDesc(Long assignedGridMemberId);

    List<PublicFeedback> findByAssignedGridMemberIdAndStatusOrderByAssignedAtDescSubmittedAtDesc(Long assignedGridMemberId, Integer status);

    List<PublicFeedback> findBySupervisorIdOrderBySubmittedAtDesc(Long supervisorId);

    /**
     * 反馈分页查询，城市与状态均可选（对应接口文档《查询公众监督反馈列表》）。
     */
    @Query("""
            SELECT feedback FROM PublicFeedback feedback
            WHERE (:cityId IS NULL OR feedback.cityId = :cityId)
              AND (:status IS NULL OR feedback.status = :status)
            """)
    Page<PublicFeedback> search(
            @Param("cityId") Long cityId,
            @Param("status") Integer status,
            Pageable pageable);

    @Query(value = """
            SELECT
                COALESCE(province.province_name, '其他') AS provinceName,
                COALESCE(province.province_name, '其他') AS provinceShortName,
                -- PM2.5 超标：按省份分组统计超标累计次数
                SUM(CASE WHEN feedback.pm25_value > 75 THEN 1 ELSE 0 END) AS pm25ExceedCount,
                -- SO2 复用同样的分组计数逻辑，只替换统计字段与阈值
                SUM(CASE WHEN feedback.so2_value > 150 THEN 1 ELSE 0 END) AS so2ExceedCount,
                -- CO 复用同样的分组计数逻辑，只替换统计字段与阈值
                SUM(CASE WHEN feedback.co_value > 4 THEN 1 ELSE 0 END) AS coExceedCount
            FROM public_feedback feedback
            LEFT JOIN grid_city city ON city.city_id = feedback.city_id
            LEFT JOIN grid_province province ON province.province_id = city.province_id
            WHERE feedback.measured_at IS NOT NULL
            GROUP BY COALESCE(province.province_name, '其他')
            ORDER BY provinceName
            """, nativeQuery = true)
    List<ProvincePollutantExceedanceProjection> countProvincePollutantExceedance();

    @Query(value = """
            SELECT
                COUNT(*) AS totalFeedback,
                SUM(CASE WHEN measured_at IS NOT NULL THEN 1 ELSE 0 END) AS completedFeedback,
                SUM(CASE WHEN CAST(submitted_at AS DATE) = CURRENT_DATE THEN 1 ELSE 0 END) AS todayFeedback
            FROM public_feedback
            """, nativeQuery = true)
    DashboardSummaryProjection countDashboardSummary();

    @Query(value = """
            SELECT
                aqi_level AS level,
                COUNT(*) AS count
            FROM public_feedback
            WHERE aqi_level IS NOT NULL AND aqi_level <> ''
            GROUP BY aqi_level
            """, nativeQuery = true)
    List<AqiDistributionProjection> countAqiDistribution();

    @Query(value = """
            SELECT
                YEAR(submitted_at) AS yearValue,
                MONTH(submitted_at) AS monthValue,
                COUNT(*) AS feedbackCount,
                SUM(CASE WHEN measured_at IS NOT NULL THEN 1 ELSE 0 END) AS completedCount,
                SUM(CASE WHEN measured_at IS NOT NULL AND pm25_value > 75 THEN 1 ELSE 0 END) AS pm25ExceedCount,
                SUM(CASE WHEN measured_at IS NOT NULL AND so2_value > 150 THEN 1 ELSE 0 END) AS so2ExceedCount,
                SUM(CASE WHEN measured_at IS NOT NULL AND co_value > 4 THEN 1 ELSE 0 END) AS coExceedCount
            FROM public_feedback
            WHERE submitted_at >= :startTime AND submitted_at < :endTime
            GROUP BY YEAR(submitted_at), MONTH(submitted_at)
            ORDER BY YEAR(submitted_at), MONTH(submitted_at)
            """, nativeQuery = true)
    List<MonthlyTrendProjection> countMonthlyTrend(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
