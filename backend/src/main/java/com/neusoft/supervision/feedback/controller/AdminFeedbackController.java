package com.neusoft.supervision.feedback.controller;

import com.neusoft.supervision.auth.security.AuthContext;
import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.feedback.dto.AdminFeedbackResponse;
import com.neusoft.supervision.feedback.dto.AssignmentCandidatesResponse;
import com.neusoft.supervision.feedback.dto.FeedbackAssignRequest;
import com.neusoft.supervision.feedback.dto.FeedbackHandleRequest;
import com.neusoft.supervision.feedback.dto.FeedbackHandleResponse;
import com.neusoft.supervision.feedback.dto.FeedbackPageResponse;
import com.neusoft.supervision.feedback.service.PublicFeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class AdminFeedbackController {
    private final PublicFeedbackService publicFeedbackService;

    public AdminFeedbackController(PublicFeedbackService publicFeedbackService) {
        this.publicFeedbackService = publicFeedbackService;
    }

    @GetMapping
    public ApiResponse<FeedbackPageResponse<AdminFeedbackResponse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(publicFeedbackService.findPage(page, size, cityId, status));
    }

    @GetMapping("/{afId}")
    public ApiResponse<AdminFeedbackResponse> detail(@PathVariable Long afId) {
        return ApiResponse.success(publicFeedbackService.findDetail(afId));
    }

    @GetMapping("/{afId}/assignment-candidates/local")
    public ApiResponse<AssignmentCandidatesResponse> localCandidates(@PathVariable Long afId) {
        return ApiResponse.success(publicFeedbackService.findLocalAssignmentCandidates(afId));
    }

    @GetMapping("/{afId}/assignment-candidates/cross-province")
    public ApiResponse<AssignmentCandidatesResponse> crossProvinceCandidates(@PathVariable Long afId) {
        return ApiResponse.success(publicFeedbackService.findCrossProvinceAssignmentCandidates(afId));
    }

    @PutMapping("/{afId}/assign")
    public ApiResponse<AdminFeedbackResponse> assign(
            @PathVariable Long afId,
            @Valid @RequestBody FeedbackAssignRequest request) {
        return ApiResponse.success(publicFeedbackService.assignGridMember(afId, request));
    }

    /**
     * 处理反馈：填写回复并办结(2)或驳回(3)。处理人由令牌确定。
     */
    @PutMapping("/{afId}/handle")
    public ApiResponse<FeedbackHandleResponse> handle(
            @PathVariable Long afId,
            @Valid @RequestBody FeedbackHandleRequest request) {
        return ApiResponse.success(
                publicFeedbackService.handleFeedback(afId, AuthContext.requireAdminId(), request));
    }
}
