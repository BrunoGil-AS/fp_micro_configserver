package com.aspiresys.fp_micro_configserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.aspiresys.fp_micro_configserver.config.ConfigServerConstants.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "allowedOriginsProperty", "http://localhost:3000,http://localhost:4200");
        ReflectionTestUtils.setField(securityConfig, "localhostIpv4", "127.0.0.1");
        ReflectionTestUtils.setField(securityConfig, "localhostIpv6", "0:0:0:0:0:0:0:1");
        ReflectionTestUtils.setField(securityConfig, "errorMessage", "Access denied");
    }

    @Test
    void whenRequestFromAllowedOrigin_thenSetCORSHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HEADER_ORIGIN, "http://localhost:3000");
        request.setRemoteAddr("127.0.0.1");

        securityConfig.corsAndLocalhostFilter().doFilter(request, response, filterChain);

        assertEquals("http://localhost:3000", response.getHeader(CORS_ALLOW_ORIGIN));
        assertEquals(CORS_METHODS_VALUE, response.getHeader(CORS_ALLOW_METHODS));
        assertEquals(CORS_HEADERS_VALUE, response.getHeader(CORS_ALLOW_HEADERS));
        assertEquals(CORS_CREDENTIALS_VALUE, response.getHeader(CORS_ALLOW_CREDENTIALS));
    }

    @Test
    void whenRequestFromLocalhost_thenAllowRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");

        securityConfig.corsAndLocalhostFilter().doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void whenRequestFromNonLocalhost_thenDenyRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setRemoteAddr("192.168.1.1");

        securityConfig.corsAndLocalhostFilter().doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        verify(filterChain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void testFilterChainConfiguration() throws Exception {
        // Crear y configurar el mock de HttpSecurity
        org.springframework.security.config.annotation.web.builders.HttpSecurity http = 
            mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
        
        // Configurar comportamiento de los mocks
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.csrf(any())).thenReturn(http);
        when(http.build()).thenReturn(new org.springframework.security.web.DefaultSecurityFilterChain(
            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/**")
        ));

        // Ejecutar el método y verificar
        SecurityFilterChain filterChain = securityConfig.filterChain(http);
        
        assertNotNull(filterChain);
        verify(http).authorizeHttpRequests(any());
        verify(http).csrf(any());
        verify(http).build();
    }
}
