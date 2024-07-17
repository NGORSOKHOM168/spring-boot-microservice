package com.learning.filter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.learning.util.JwtUtil;

import reactor.core.publisher.Mono;

@Component
public class CustomGatewayFilter implements GatewayFilter {

	@Autowired
	private RouteValidator validator;
	
    @Autowired
    private JwtUtil jwtUtil;
    
    private static final int REQUEST_LIMIT = 1;
    private static final int WINDOW_SIZE_SECONDS = 1;
    private static final Map<String, RequestRateLimiter> requestCounters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Validate token
        if (validator.isSecured.test(request)) {
            if (authMissing(request)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
            final String token = request.getHeaders().getOrEmpty("Authorization").get(0);
            if (jwtUtil.isExpiredToken(token)) {            	
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        }
        // Validate to allow one request per second
        String clientIp = getClientIp(exchange); 
        RequestRateLimiter rateLimiter = requestCounters.computeIfAbsent(clientIp, ip -> new RequestRateLimiter());
        if (!rateLimiter.isAllowedRequest()) {
        	 exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
             return exchange.getResponse().setComplete();
        }     

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private boolean authMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }   
    
    private String getClientIp(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route.getUri().getHost();
    }

    private static class RequestRateLimiter {
	    private long lastRequestTime = 0;
        private int requestCount = 0;
        public boolean isAllowedRequest() {
        	long currentTime = Instant.now().getEpochSecond();
            if (currentTime - lastRequestTime > WINDOW_SIZE_SECONDS) {
                requestCount = 1;
                lastRequestTime = currentTime;
                return true;
            }
            if (requestCount < REQUEST_LIMIT) {
                requestCount++;
                lastRequestTime = currentTime;
                return true;
            }

            return false;
        }
    }
}