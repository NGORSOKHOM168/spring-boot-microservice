package com.learning.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learning.filter.CustomGatewayFilter;



@Configuration
@EnableHystrix
public class GatewayConfig {
		
	@Autowired
    private CustomGatewayFilter customfilter;

    @Bean
    RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()        		
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f.filter(customfilter))
                        .uri("lb://auth-service"))
//                .route("account-service", r -> r.path("/api/v1/account/**")
//                        .filters(f -> f.filter(customfilter))
//                        .uri("lb://user-service"))
                .build();
    }
}
