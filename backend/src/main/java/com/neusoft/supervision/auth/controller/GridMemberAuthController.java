package com.neusoft.supervision.auth.controller;

import com.neusoft.supervision.auth.dto.GridMemberLoginRequest;
import com.neusoft.supervision.auth.dto.GridMemberLoginResponse;
import com.neusoft.supervision.auth.service.GridMemberAuthService;
import com.neusoft.supervision.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/grid-member")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class GridMemberAuthController {
    private final GridMemberAuthService gridMemberAuthService;

    public GridMemberAuthController(GridMemberAuthService gridMemberAuthService) {
        this.gridMemberAuthService = gridMemberAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<GridMemberLoginResponse> login(@Valid @RequestBody GridMemberLoginRequest request) {
        return ApiResponse.success(gridMemberAuthService.login(request));
    }
}
