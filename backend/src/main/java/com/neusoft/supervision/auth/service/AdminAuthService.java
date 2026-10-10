package com.neusoft.supervision.auth.service;

import com.neusoft.supervision.auth.dto.AdminInfo;
import com.neusoft.supervision.auth.dto.AdminLoginRequest;
import com.neusoft.supervision.auth.dto.AdminLoginResponse;
import com.neusoft.supervision.auth.entity.SysAdmin;
import com.neusoft.supervision.auth.repository.SysAdminRepository;
import com.neusoft.supervision.auth.security.AuthPrincipal;
import com.neusoft.supervision.auth.security.AuthRole;
import com.neusoft.supervision.auth.security.JwtTokenService;
import com.neusoft.supervision.feedback.support.StatusFlags;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 管理员认证。管理员账号存于 sys_admin 表，密码以 BCrypt 摘要存储。
 */
@Service
public class AdminAuthService {
    private final SysAdminRepository sysAdminRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AdminAuthService(
            SysAdminRepository sysAdminRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.sysAdminRepository = sysAdminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        String username = request.getAdminCode().trim().toLowerCase(Locale.ROOT);

        SysAdmin admin = sysAdminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("管理员账号或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("管理员账号或密码错误");
        }

        if (!Integer.valueOf(StatusFlags.ENABLED).equals(admin.getStatus())) {
            throw new IllegalArgumentException("该管理员账号已被停用");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        sysAdminRepository.save(admin);

        AdminInfo adminInfo = new AdminInfo(admin.getAdminId(), admin.getUsername(), resolveDisplayName(admin));
        AuthPrincipal principal = new AuthPrincipal(
                admin.getAdminId(), admin.getUsername(), AuthRole.ADMIN);

        return new AdminLoginResponse(jwtTokenService.issueToken(principal), adminInfo);
    }

    /**
     * 展示名优先取真实姓名；处理反馈时也用它作为处理人姓名。
     */
    private String resolveDisplayName(SysAdmin admin) {
        if (admin.getRealName() != null && !admin.getRealName().isBlank()) {
            return admin.getRealName();
        }

        return admin.getUsername();
    }
}
