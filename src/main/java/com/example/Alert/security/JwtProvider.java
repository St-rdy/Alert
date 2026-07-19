package com.example.Alert.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// JWT 토큰을 검증하고 클레임을 추출하는 유틸리티 클래스
// Node.js의 jwt.verify() + jwt.decode() 역할을 담당
@Component
public class JwtProvider {

    // application-local.properties의 jwt.secret 값을 주입
    // HMAC-SHA256은 최소 32바이트 키 필요
    @Value("${jwt.secret}")
    private String jwtSecret;

    // 토큰의 서명과 만료 여부를 검증
    // 실패 시 false 반환 → 필터에서 인증을 설정하지 않음 → Spring Security가 401 처리
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey())    // 서명 검증에 사용할 키 지정
                    .build()
                    .parseSignedClaims(token);  // 서명 + 만료 시간 동시 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 검증된 토큰에서 userId(클레임 키: "id") 추출
    // JWT payload의 "id"는 Integer로 파싱될 수 있어 Number로 받아 longValue()로 변환
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // Claims 객체 = JWT payload
        return ((Number) claims.get("id")).longValue();
    }

    // jwt.secret 문자열을 HMAC-SHA256 키 객체로 변환
    // Keys.hmacShaKeyFor()가 키 길이 유효성도 검사함
    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
