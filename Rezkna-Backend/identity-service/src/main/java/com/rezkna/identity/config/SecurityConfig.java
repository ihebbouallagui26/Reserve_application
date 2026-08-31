package com.rezkna.identity.config;

import com.rezkna.common.security.JwtAuthenticationEntryPoint;
import com.rezkna.common.security.JwtAuthenticationFilter;
import com.rezkna.common.security.RestAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                                            RestAccessDeniedHandler restAccessDeniedHandler) throws Exception {
        http
                // Stateless REST API authenticated via a Bearer JWT, not browser cookies/sessions.
                // CSRF protects session-cookie based auth from cross-site form submission; that
                // attack does not apply here, so it is safe (and standard practice) to disable.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/config",
                                "/register",
                                "/login",
                                "/social",
                                "/phone/start",
                                "/phone/verify",
                                "/partner/login",
                                "/platform/login"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
