package com.neusoft.supervision.feedback.controller;

import com.neusoft.supervision.auth.security.AuthContext;
import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.feedback.dto.GridMeasurementSubmitRequest;
import com.neusoft.supervision.feedback.dto.GridTaskResponse;
import com.neusoft.supervision.feedback.service.PublicFeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grid/tasks")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class GridTaskController {
    private final PublicFeedbackService publicFeedbackService;

    public GridTaskController(PublicFeedbackService publicFeedbackService) {
        this.publicFeedbackService = publicFeedbackService;
    }

    @GetMapping
    public ApiResponse<List<GridTaskResponse>> list(@RequestParam(required = false) Integer status) {
        return ApiResponse.success(publicFeedbackService.findGridTasks(AuthContext.requireGridMemberId(), status));
    }

    @GetMapping("/{afId}")
    public ApiResponse<GridTaskResponse> detail(@PathVariable Long afId) {
        return ApiResponse.success(publicFeedbackService.findGridTaskDetail(afId, AuthContext.requireGridMemberId()));
    }

    @PostMapping("/{afId}/measurements")
    public ApiResponse<GridTaskResponse> submit(
            @PathVariable Long afId,
            @Valid @RequestBody GridMeasurementSubmitRequest request) {
        return ApiResponse.success(publicFeedbackService.submitMeasurement(afId, AuthContext.requireGridMemberId(), request));
    }
}
