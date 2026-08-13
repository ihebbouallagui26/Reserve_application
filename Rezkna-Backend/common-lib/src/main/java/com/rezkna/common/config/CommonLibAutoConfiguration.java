package com.rezkna.common.config;

import com.rezkna.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Registers common-lib's beans regardless of the consuming service's base package.
 * Spring Boot's component scan only covers the main application class's package and
 * its sub-packages, so a bean living in com.rezkna.common would otherwise never be
 * picked up by e.g. com.rezkna.identity. Auto-configuration is the standard mechanism
 * Spring Boot "starter" libraries use to avoid this exact problem.
 */
@AutoConfiguration
public class CommonLibAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
