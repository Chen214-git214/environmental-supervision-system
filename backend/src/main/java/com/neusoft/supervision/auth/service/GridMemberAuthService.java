package com.neusoft.supervision.auth.service;

import com.neusoft.supervision.auth.dto.GridMemberInfo;
import com.neusoft.supervision.auth.dto.GridMemberLoginRequest;
import com.neusoft.supervision.auth.dto.GridMemberLoginResponse;
import com.neusoft.supervision.auth.security.AuthPrincipal;
import com.neusoft.supervision.auth.security.AuthRole;
import com.neusoft.supervision.auth.security.JwtTokenService;
import com.neusoft.supervision.feedback.entity.GridMember;
import com.neusoft.supervision.feedback.repository.GridMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class GridMemberAuthService {
    private static final int GRID_MEMBER_ON_DUTY = 1;
    private static final String DEFAULT_PASSWORD = "123";

    private final GridMemberRepository gridMemberRepository;
    private final JwtTokenService jwtTokenService;

    public GridMemberAuthService(GridMemberRepository gridMemberRepository, JwtTokenService jwtTokenService) {
        this.gridMemberRepository = gridMemberRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public GridMemberLoginResponse login(GridMemberLoginRequest request) {
        if (!DEFAULT_PASSWORD.equals(request.getPassword())) {
            throw new IllegalArgumentException("网格员账号或密码错误");
        }

        GridMember gridMember = gridMemberRepository
                .findByEmployeeNoAndStatus(request.getEmployeeNo(), GRID_MEMBER_ON_DUTY)
                .orElseThrow(() -> new IllegalArgumentException("网格员账号或密码错误"));

        GridMemberInfo info = toInfo(gridMember);
        AuthPrincipal principal = new AuthPrincipal(
                gridMember.getGridMemberId(),
                gridMember.getEmployeeNo(),
                AuthRole.GRID_MEMBER);

        return new GridMemberLoginResponse(jwtTokenService.issueToken(principal), info);
    }

    private GridMemberInfo toInfo(GridMember gridMember) {
        return new GridMemberInfo(
                gridMember.getGridMemberId(),
                gridMember.getEmployeeNo(),
                gridMember.getRealName(),
                gridMember.getPhone(),
                gridMember.getProvinceName(),
                gridMember.getCityName(),
                gridMember.getGridName());
    }
}
