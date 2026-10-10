package com.neusoft.supervision.statistics.controller;

import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.statistics.dto.AirQualityStatQuery;
import com.neusoft.supervision.statistics.dto.AirQualityStatResponse;
import com.neusoft.supervision.statistics.service.AirQualityStatQueryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 查询空气质量监测数据统计，对应接口文档《查询空气质量监测数据统计》。
 * 数据来自 air_quality_stat 表（网格员实测上报）。
 */
@RestController
@RequestMapping("/api/air-quality-stats")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class AirQualityStatController {
    private final AirQualityStatQueryService airQualityStatQueryService;

    public AirQualityStatController(AirQualityStatQueryService airQualityStatQueryService) {
        this.airQualityStatQueryService = airQualityStatQueryService;
    }

    @GetMapping
    public ApiResponse<List<AirQualityStatResponse>> list(AirQualityStatQuery query) {
        return ApiResponse.success(airQualityStatQueryService.search(query));
    }
}
