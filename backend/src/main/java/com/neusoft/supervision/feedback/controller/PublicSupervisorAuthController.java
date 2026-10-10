package com.neusoft.supervision.feedback.controller;

import com.neusoft.supervision.auth.service.PublicSupervisorAuthService;
import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.feedback.dto.PublicSupervisorAuthResponse;
import com.neusoft.supervision.feedback.dto.PublicSupervisorLoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/public-supervisor")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class PublicSupervisorAuthController {
    private final PublicSupervisorAuthService publicSupervisorAuthService;

    public PublicSupervisorAuthController(PublicSupervisorAuthService publicSupervisorAuthService) {
        this.publicSupervisorAuthService = publicSupervisorAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<PublicSupervisorAuthResponse> login(
            @Valid @RequestBody PublicSupervisorLoginRequest request) {
        return ApiResponse.success(publicSupervisorAuthService.login(request));
    }
}
