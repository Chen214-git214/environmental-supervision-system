package com.neusoft.supervision.statistics.controller;

import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.statistics.dto.DashboardResponse;
import com.neusoft.supervision.statistics.service.DashboardStatisticsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class DashboardStatisticsController {
    private final DashboardStatisticsService dashboardStatisticsService;

    public DashboardStatisticsController(DashboardStatisticsService dashboardStatisticsService) {
        this.dashboardStatisticsService = dashboardStatisticsService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard() {
        return ApiResponse.success(dashboardStatisticsService.dashboard());
    }
}
