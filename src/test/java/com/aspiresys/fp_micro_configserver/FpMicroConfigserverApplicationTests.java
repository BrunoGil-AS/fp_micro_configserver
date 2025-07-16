package com.aspiresys.fp_micro_configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.config.server.environment.EnvironmentController;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FpMicroConfigserverApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> 0); // Puerto aleatorio
        registry.add("spring.cloud.config.server.git.uri", () -> "file://./config-repo");
        registry.add("spring.cloud.config.server.git.default-label", () -> "main");
    }

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void configServerIsEnabled() {
        assertNotNull(applicationContext.getBean(EnvironmentController.class));
    }

    @Test
    void applicationStartsSuccessfully() {
        assertTrue(port > 0, "Application should start with a valid port");
        assertNotNull(applicationContext.getBean(FpMicroConfigserverApplication.class));
    }
}
