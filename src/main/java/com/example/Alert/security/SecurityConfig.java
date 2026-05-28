package com.example.Alert.security;

import com.example.Alert.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 직접 만든 JWT 필터를 주입받아 필터 체인에 등록
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // JWT 기반 인증은 서버에 세션을 저장하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // Spring의 기본 UsernamePasswordAuthenticationFilter 앞에 JWT 필터 삽입
                // 이 위치에 넣어야 JWT 인증이 Spring Security 기본 처리보다 먼저 실행됨
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 인증 실패(토큰 없음/만료) 시 401 + JSON 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            ApiResponse<Void> body = ApiResponse.error(401, "UNAUTHORIZED", "인증이 필요합니다.");
                            response.getWriter().write(objectMapper.writeValueAsString(body));
                        }));

        return http.build();
    }
}
