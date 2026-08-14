package com.rezkna.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rezkna.common.security.JwtAuthenticationEntryPoint;
import com.rezkna.common.security.JwtAuthenticationFilter;
import com.rezkna.common.security.JwtService;
import com.rezkna.common.security.RestAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Registers the shared JWT validation beans regardless of the consuming service's
 * base package - same reasoning as CommonLibAutoConfiguration.
 */
@AutoConfiguration
public class JwtAutoConfiguration {

    @Bean
    public JwtService jwtService(@Value("${jwt.secret}") String jwtSecret) {
        return new JwtService(jwtSecret);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
        return new RestAccessDeniedHandler(objectMapper);
    }
}
