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
    
    /**
     * Configures the main security filter chain for the application.
     * <p>
     * This bean sets up the following security rules:
     * <ul>
     *   <li>Disables Cross-Site Request Forgery (CSRF) protection, as this service is not directly accessed by browsers in a stateful way.</li>
     *   <li>Permits all incoming HTTP requests at the Spring Security level. The actual access control is delegated to the {@link #corsAndLocalhostFilter()}, which restricts requests to localhost.</li>
     * </ul>
     * This broad permission is necessary to allow the custom filter to handle all incoming traffic for fine-grained control.
     * </p>
     *
     * @param http The {@link HttpSecurity} to configure.
     * @return The configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
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

    /**
     * Creates a filter to handle CORS and restrict access to localhost.
     * <p>
     * This filter is executed with the highest precedence to ensure it runs before any other security or application filters. It performs two primary functions:
     * <ol>
     *   <li><b>CORS Handling</b>: It inspects the {@code Origin} header of the incoming request. If the origin is in the list of allowed origins (configured via {@code app.allowed.origins}), it adds the necessary CORS headers to the response, allowing cross-origin requests from trusted sources.</li>
     *   <li><b>Localhost Restriction</b>: It checks the remote IP address of the request. Access is only granted if the request originates from the configured localhost IPv4 or IPv6 addresses. For any other address, it rejects the request with an HTTP 403 Forbidden status and a configured error message.</li>
     * </ol>
     * This ensures that the configuration server can only be accessed by services running on the same machine, enhancing security.
     * </p>
     *
     * @return A {@link Filter} instance that enforces CORS and localhost-only access.
     */
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
