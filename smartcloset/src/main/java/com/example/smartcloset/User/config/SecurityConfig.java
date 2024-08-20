package com.example.smartcloset.User.config;

import com.example.smartcloset.User.security.JwtRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
        System.out.println("JwtRequestFilter instance injected: " + jwtRequestFilter);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("Configuring SecurityFilterChain...");
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .cors(withDefaults()) // 추가된 CORS 설정
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/api/users/login").permitAll()
                                .requestMatchers("/api/users/register").permitAll()
                                .requestMatchers("/api/users/check/nickname").permitAll()
                                .requestMatchers("/api/users/check/loginId").permitAll()
                        .requestMatchers("/api/users/change-password").authenticated()  // 인증된 사용자만 접근 가능
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // 서버 세션 유지
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));


        // JwtRequestFilter를 UsernamePasswordAuthenticationFilter 전에 추가합니다.
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);


        System.out.println("SecurityFilterChain configured.");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("Configuring PasswordEncoder...");
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
}
