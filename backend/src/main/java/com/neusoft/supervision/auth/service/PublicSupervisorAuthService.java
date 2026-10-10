package com.neusoft.supervision.auth.service;

import com.neusoft.supervision.auth.security.AuthPrincipal;
import com.neusoft.supervision.auth.security.AuthRole;
import com.neusoft.supervision.auth.security.JwtTokenService;
import com.neusoft.supervision.feedback.dto.PublicSupervisorAuthResponse;
import com.neusoft.supervision.feedback.dto.PublicSupervisorInfo;
import com.neusoft.supervision.feedback.dto.PublicSupervisorLoginRequest;
import com.neusoft.supervision.feedback.dto.PublicSupervisorRegisterRequest;
import com.neusoft.supervision.feedback.entity.PublicSupervisor;
import com.neusoft.supervision.feedback.repository.PublicSupervisorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class PublicSupervisorAuthService {
    private static final int STATUS_ENABLED = 1;

    private final PublicSupervisorRepository publicSupervisorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public PublicSupervisorAuthService(
            PublicSupervisorRepository publicSupervisorRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.publicSupervisorRepository = publicSupervisorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public PublicSupervisorAuthResponse register(PublicSupervisorRegisterRequest request) {
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);
        String phone = request.getPhone().trim();

        if (publicSupervisorRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("该登录账号已被注册");
        }

        if (publicSupervisorRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        PublicSupervisor supervisor = new PublicSupervisor();
        supervisor.setUsername(username);
        supervisor.setPassword(passwordEncoder.encode(request.getPassword()));
        supervisor.setRealName(request.getRealName().trim());
        supervisor.setPhone(phone);
        supervisor.setIdCard(normalizeIdCard(request.getIdCard()));
        supervisor.setStatus(STATUS_ENABLED);
        supervisor.setRegisterTime(LocalDateTime.now());

        return toAuthResponse(publicSupervisorRepository.save(supervisor));
    }

    @Transactional(readOnly = true)
    public PublicSupervisorAuthResponse login(PublicSupervisorLoginRequest request) {
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);
        PublicSupervisor supervisor = publicSupervisorRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("监督员账号或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), supervisor.getPassword())) {
            throw new IllegalArgumentException("监督员账号或密码错误");
        }

        if (!Integer.valueOf(STATUS_ENABLED).equals(supervisor.getStatus())) {
            throw new IllegalArgumentException("该账号已被停用，请联系管理员");
        }

        return toAuthResponse(supervisor);
    }

    /**
     * 供拦截器与业务层查询当前登录账号是否仍然有效（存在且启用）。
     */
    @Transactional(readOnly = true)
    public PublicSupervisorInfo findActiveInfo(Long supervisorId) {
        PublicSupervisor supervisor = publicSupervisorRepository.findById(supervisorId)
                .orElseThrow(() -> new IllegalArgumentException("监督员账号不存在"));

        if (!Integer.valueOf(STATUS_ENABLED).equals(supervisor.getStatus())) {
            throw new IllegalArgumentException("该账号已被停用，请联系管理员");
        }

        return toInfo(supervisor);
    }

    private PublicSupervisorAuthResponse toAuthResponse(PublicSupervisor supervisor) {
        AuthPrincipal principal = new AuthPrincipal(
                supervisor.getSupervisorId(),
                supervisor.getUsername(),
                AuthRole.PUBLIC_SUPERVISOR);

        return new PublicSupervisorAuthResponse(jwtTokenService.issueToken(principal), toInfo(supervisor));
    }

    private PublicSupervisorInfo toInfo(PublicSupervisor supervisor) {
        return new PublicSupervisorInfo(
                supervisor.getSupervisorId(),
                supervisor.getUsername(),
                supervisor.getRealName(),
                supervisor.getPhone(),
                supervisor.getStatus());
    }

    private String normalizeIdCard(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            return null;
        }

        return idCard.trim().toUpperCase(Locale.ROOT);
    }
}
