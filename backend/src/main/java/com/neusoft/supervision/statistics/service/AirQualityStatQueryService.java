package com.neusoft.supervision.statistics.service;

import com.neusoft.supervision.feedback.repository.AirQualityStatRepository;
import com.neusoft.supervision.statistics.dto.AirQualityStatQuery;
import com.neusoft.supervision.statistics.dto.AirQualityStatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实测数据查询。数据来源为 air_quality_stat 表。
 */
@Service
public class AirQualityStatQueryService {
    private final AirQualityStatRepository airQualityStatRepository;

    public AirQualityStatQueryService(AirQualityStatRepository airQualityStatRepository) {
        this.airQualityStatRepository = airQualityStatRepository;
    }

    @Transactional(readOnly = true)
    public List<AirQualityStatResponse> search(AirQualityStatQuery query) {
        LocalDateTime startTime = query.getStartTime();
        LocalDateTime endTime = query.getEndTime();

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        return airQualityStatRepository.search(
                query.getCityId(), query.getGridMemberId(), startTime, endTime);
    }
}
