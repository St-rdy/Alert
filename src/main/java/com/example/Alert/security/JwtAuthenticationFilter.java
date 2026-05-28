package com.example.Alert.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// 모든 요청에 한 번씩 실행되는 JWT 인증 필터
// Node.js의 authMiddleware와 동일한 역할
// OncePerRequestFilter: 리다이렉트 등으로 요청이 두 번 처리돼도 필터는 한 번만 실행됨을 보장
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 "Bearer <token>" 형태로 토큰 추출
        String token = extractToken(request);

        // 토큰이 있고 유효하면 SecurityContext에 인증 정보 저장
        // 이후 컨트롤러에서 @AuthenticationPrincipal Long userId 로 꺼낼 수 있음
        if (token != null && jwtProvider.validateToken(token)) {
            Long userId = jwtProvider.getUserId(token);

            // principal 자리에 userId(Long)를 넣음
            // credentials(두 번째)는 불필요하므로 null
            // authorities(세 번째)는 역할 구분이 없으므로 빈 리스트
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 인증 여부와 관계없이 다음 필터로 넘김
        // 인증이 없으면 Spring Security가 이후 단계에서 401 처리
        filterChain.doFilter(request, response);
    }

    // "Bearer " 접두사 제거 후 순수 토큰 문자열 반환
    // 헤더가 없거나 형식이 맞지 않으면 null 반환
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
