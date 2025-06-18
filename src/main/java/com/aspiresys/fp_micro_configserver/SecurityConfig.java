package com.aspiresys.fp_micro_configserver;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Value("${app.allowed.origins}")
    private String allowedOriginsProperty;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter corsAndLocalhostFilter() {
        return new OncePerRequestFilter() {
            private final List<String> allowedOrigins = Arrays.asList(allowedOriginsProperty.split(","));
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
                String origin = request.getHeader("Origin");
                if (origin != null && allowedOrigins.contains(origin)) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                    response.setHeader("Access-Control-Allow-Headers", "* ");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                }
                // Permitir solo peticiones locales
                String remoteAddr = request.getRemoteAddr();
                if (remoteAddr.equals("127.0.0.1") || remoteAddr.equals("0:0:0:0:0:0:0:1")) {
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo se permiten peticiones locales");
                }
            }
        };
    }
}
