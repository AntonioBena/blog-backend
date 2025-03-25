package com.job.interview.blog.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfiguration {

    private final ApplicationProperties appProperties;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping(
                                appProperties
                                        .getSecurity()
                                        .getCorsConfiguration()
                                        .pattern()
                        )
                        .allowedOrigins(
                                appProperties
                                        .getSecurity()
                                        .getAllowedOrigins()
                        )
                        .allowedMethods(
                                appProperties
                                        .getSecurity()
                                        .getAllowedMethods()
                        )
                        .allowedHeaders(
                                appProperties
                                        .getSecurity()
                                        .getAllowedHeaders()
                        )
                        .allowCredentials(true);
            }
        };
    }
}