package com.example.smartcloset.User.config;

import com.example.smartcloset.User.security.JwtRequestFilter;
import com.example.smartcloset.User.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
                        configuration.setExposedHeaders(Arrays.asList("Authorization"));
                        configuration.setMaxAge(3600L);
                        return configuration;
                    }
                }))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // 유저 관련 요청
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/naver/login").permitAll()
                        .requestMatchers("/api/users/kakao/login").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/check/nickname").permitAll()
                        .requestMatchers("/api/users/check/loginId").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/bot/chat").permitAll()
                        // 게시판 관련 요청
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll() // 모든 사용자가 게시글 조회 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts/user").authenticated() // 사용자 자신의 게시글 조회는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/posts/{id}").permitAll() // 특정 게시글 조회는 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/posts/search").permitAll() // 게시글 검색은 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/posts/loadMore").permitAll() // 무한 스크롤은 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/comments").permitAll() // 특정 게시글 댓글 조회는 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/posts/liked").authenticated() // 사용자가 좋아요한 게시글 조회는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/posts/liked/scroll").authenticated() // 스크롤로 좋아요한 게시글 조회는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()  // 게시글 생성은 인증된 사용자만 허용
                        .requestMatchers(HttpMethod.POST, "/api/posts/withImage").authenticated() // 이미지 포함 게시글 작성은 인증된 사용자만 허용
                        .requestMatchers(HttpMethod.PUT, "/api/posts/{id}").authenticated() // 게시글 수정은 인증된 사용자만 허용
                        .requestMatchers(HttpMethod.PUT, "/api/posts/{id}/like").authenticated() // 게시글 좋아요 추가는 인증된 사용자만 허용
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}/like").authenticated() // 게시글 좋아요 취소는 인증된 사용자만 허용
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}").authenticated() // 게시글 삭제는 인증된 사용자만 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 세션 상태 유지하지 않음




        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied!");
        };
    }

    @Bean
    public Http403ForbiddenEntryPoint authenticationEntryPoint() {
        return new Http403ForbiddenEntryPoint();
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService();
    }
}
