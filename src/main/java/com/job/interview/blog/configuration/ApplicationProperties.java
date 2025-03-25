package com.job.interview.blog.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    @NotNull
    private final Security security;
    @NotNull
    private final Date date;
    @NotNull
    private final Validation validation;

    public ApplicationProperties(Security security, Date date, Validation validation) {
        this.security = security;
        this.date = date;
        this.validation = validation;
    }

    public record Validation(
            @NotBlank(message = "Email regex is mandatory!")
            String emailRegex,
            @NotBlank(message = "Date regex is mandatory!")
            String dateRegex,
            @NotBlank(message = "Registration number regex is mandatory!")
            String registrationNumberRegex
    ) {
    }

    @Valid
    public record Date(
            @NotBlank
            String format
    ) {
    }

    @Getter
    @AllArgsConstructor
    public static class Security {

        private final boolean createEnabledUsers; //TODO add configs for spring security later
        private final String[] allowedHeaders;
        private final String[] allowedOrigins;
        private final String[] allowedMethods;
        private final String[] requestMatchers;
        private final CorsConfiguration corsConfiguration;
        private Boolean csrfEnabled;

        public record CorsConfiguration(
                @NotBlank
                @Length(min = 3, message = "Cors pattern should be at least 3 chars long!")
                String pattern
        ) {}
    }
}
