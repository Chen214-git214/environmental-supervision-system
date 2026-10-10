package com.neusoft.supervision.feedback.controller;

import com.neusoft.supervision.auth.security.AuthContext;
import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.feedback.dto.MyFeedbackResponse;
import com.neusoft.supervision.feedback.dto.PublicFeedbackCreateRequest;
import com.neusoft.supervision.feedback.dto.PublicFeedbackResponse;
import com.neusoft.supervision.feedback.service.PublicFeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public-feedback")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class PublicFeedbackController {
    private final PublicFeedbackService publicFeedbackService;

    public PublicFeedbackController(PublicFeedbackService publicFeedbackService) {
        this.publicFeedbackService = publicFeedbackService;
    }

    /**
     * 提交反馈。提交人由令牌确定，请求体不再接收 supervisorId，避免以他人身份提交。
     */
    @PostMapping
    public ApiResponse<PublicFeedbackResponse> create(@Valid @RequestBody PublicFeedbackCreateRequest request) {
        return ApiResponse.success(
                publicFeedbackService.create(AuthContext.requirePublicSupervisorId(), request));
    }

    /**
     * 查看自己提交的反馈及处理进展。
     */
    @GetMapping("/mine")
    public ApiResponse<List<MyFeedbackResponse>> mine() {
        return ApiResponse.success(publicFeedbackService.findMyFeedback(AuthContext.requirePublicSupervisorId()));
    }
}
