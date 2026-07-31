package com.hireforge.api_gateway.config;

import com.hireforge.api_gateway.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/auth-service/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(jwtAuthenticationFilter.apply(new Object()))
                        )
                        .uri("lb://AUTH-SERVICE")
                )
                .route("resume-service", r -> r
                        .path("/resume-service/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(jwtAuthenticationFilter.apply(new Object()))
                        )
                        .uri("lb://RESUME-SERVICE")
                )
                .route("job-tracker-service", r -> r
                .path("/job-tracker-service/**")
                .filters(f -> f
                        .stripPrefix(1)
                        .filter(jwtAuthenticationFilter.apply(new Object()))
                )
                .uri("lb://JOB-TRACKER-SERVICE")
)
                .build();
    }
}