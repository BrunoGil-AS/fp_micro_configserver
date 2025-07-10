package com.aspiresys.fp_micro_configserver.config;

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

import static com.aspiresys.fp_micro_configserver.config.ConfigServerConstants.*;

/**
 * Security configuration class for the application.
 * <p>
 * This class sets up security filters and CORS (Cross-Origin Resource Sharing) policies.
 * It permits all HTTP requests but restricts access to only local requests based on configured IPv4 and IPv6 addresses.
 * CORS headers are set for allowed origins specified in the application properties.
 * </p>
 *
 * <p>
 * Properties used:
 * <ul>
 *   <li><b>app.allowed.origins</b>: Comma-separated list of allowed CORS origins.</li>
 *   <li><b>app.security.localhost.ipv4</b>: IPv4 address considered as localhost.</li>
 *   <li><b>app.security.localhost.ipv6</b>: IPv6 address considered as localhost.</li>
 *   <li><b>app.security.error.message</b>: Error message returned for forbidden requests.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Beans defined:
 * <ul>
 *   <li><b>SecurityFilterChain</b>: Configures HTTP security, disables CSRF, and permits all requests.</li>
 *   <li><b>corsAndLocalhostFilter</b>: A filter that sets CORS headers for allowed origins and restricts access to local requests only.</li>
 * </ul>
 * </p>
 */
@Configuration
public class SecurityConfig {
    @Value("${app.allowed.origins}")
    private String allowedOriginsProperty;
    
    @Value("${app.security.localhost.ipv4}")
    private String localhostIpv4;
    
    @Value("${app.security.localhost.ipv6}")
    private String localhostIpv6;
    
    @Value("${app.security.error.message}")
    private String errorMessage;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(new AntPathRequestMatcher(PERMIT_ALL_PATTERN)).permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter corsAndLocalhostFilter() {
        return new OncePerRequestFilter() {
            private final List<String> allowedOrigins = Arrays.asList(allowedOriginsProperty.split(ORIGINS_DELIMITER));
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
                String origin = request.getHeader(HEADER_ORIGIN);
                if (origin != null && allowedOrigins.contains(origin)) {
                    response.setHeader(CORS_ALLOW_ORIGIN, origin);
                    response.setHeader(CORS_ALLOW_METHODS, CORS_METHODS_VALUE);
                    response.setHeader(CORS_ALLOW_HEADERS, CORS_HEADERS_VALUE);
                    response.setHeader(CORS_ALLOW_CREDENTIALS, CORS_CREDENTIALS_VALUE);
                }
                // Permitir solo peticiones locales
                String remoteAddr = request.getRemoteAddr();
                if (remoteAddr.equals(localhostIpv4) || remoteAddr.equals(localhostIpv6)) {
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, errorMessage);
                }
            }
        };
    }
}
