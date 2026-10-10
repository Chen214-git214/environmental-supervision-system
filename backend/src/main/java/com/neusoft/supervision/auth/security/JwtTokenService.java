package com.neusoft.supervision.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 的签发与校验。
 *
 * <p>令牌使用 HS256 对称签名，载荷包含用户编号（uid）、角色（role）与过期时间（exp）。
 * 签名密钥由配置项 {@code supervision.jwt.secret} 提供（Base64 编码，长度需满足 HS256 的 256 位要求）。</p>
 */
@Component
public class JwtTokenService {
    private final SecretKey secretKey;
    private final Duration adminTtl;
    private final Duration gridMemberTtl;

    public JwtTokenService(
            @Value("${supervision.jwt.secret}") String base64Secret,
            @Value("${supervision.jwt.admin-ttl-minutes:120}") long adminTtlMinutes,
            @Value("${supervision.jwt.grid-member-ttl-minutes:720}") long gridMemberTtlMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.adminTtl = Duration.ofMinutes(adminTtlMinutes);
        this.gridMemberTtl = Duration.ofMinutes(gridMemberTtlMinutes);
    }

    public String issueToken(AuthPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ttlOf(principal.role()));

        return Jwts.builder()
                .subject(principal.account())
                .claim("uid", principal.userId())
                .claim("role", principal.role())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 校验签名与有效期并返回身份信息。任何解析失败都统一抛 {@link UnauthorizedException}，避免向调用方泄露失败细节。
     */
    public AuthPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = claims.get("role", String.class);
            Number userId = claims.get("uid", Number.class);

            if (role == null || userId == null) {
                throw new UnauthorizedException("登录状态无效，请重新登录");
            }

            return new AuthPrincipal(userId.longValue(), claims.getSubject(), role);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("登录状态已失效，请重新登录");
        }
    }

    private Duration ttlOf(String role) {
        // 管理端是内部办公场景，有效期短；网格员与公众监督员是移动端，适当放宽
        return AuthRole.ADMIN.equals(role) ? adminTtl : gridMemberTtl;
    }
}
