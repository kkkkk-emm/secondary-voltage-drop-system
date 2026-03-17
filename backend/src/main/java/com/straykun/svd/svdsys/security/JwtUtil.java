package com.straykun.svd.svdsys.security;

import com.straykun.svd.svdsys.domain.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:svd-secret-key-2025-svd-secret-key-2025}")
    private String secret;

    @Value("${jwt.expire-seconds:7200}")
    private long expireSeconds;

    /**
     * 执行 generateToken 业务逻辑。
     *
     * @param user 参数 user。
     * @return 返回字符串结果。
     */
    public String generateToken(SysUser user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 执行 parseToken 数据处理。
     *
     * @param token 参数 token。
     * @return 返回处理结果。
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
