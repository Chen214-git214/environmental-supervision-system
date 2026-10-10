package com.neusoft.supervision.feedback.controller;

import com.neusoft.supervision.auth.security.AuthContext;
import com.neusoft.supervision.auth.service.PublicSupervisorAuthService;
import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.feedback.dto.PublicSupervisorAuthResponse;
import com.neusoft.supervision.feedback.dto.PublicSupervisorInfo;
import com.neusoft.supervision.feedback.dto.PublicSupervisorLoginRequest;
import com.neusoft.supervision.feedback.dto.PublicSupervisorRegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public-supervisors")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class PublicSupervisorController {
    private final PublicSupervisorAuthService publicSupervisorAuthService;

    public PublicSupervisorController(PublicSupervisorAuthService publicSupervisorAuthService) {
        this.publicSupervisorAuthService = publicSupervisorAuthService;
    }

    /**
     * 注册接口本身公开（用户尚无账号），注册成功即签发令牌，前端可直接进入反馈流程。
     */
    @PostMapping
    public ApiResponse<PublicSupervisorAuthResponse> register(
            @Valid @RequestBody PublicSupervisorRegisterRequest request) {
        return ApiResponse.success(publicSupervisorAuthService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<PublicSupervisorInfo> current() {
        return ApiResponse.success(publicSupervisorAuthService.findActiveInfo(AuthContext.requirePublicSupervisorId()));
    }
}
