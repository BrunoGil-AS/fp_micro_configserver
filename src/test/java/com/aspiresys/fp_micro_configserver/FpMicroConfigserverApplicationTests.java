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

/**
 * Integration tests for the {@link FpMicroConfigserverApplication}.
 * <p>
 * These tests load the full Spring application context and verify that the config server starts correctly and its key components are available.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FpMicroConfigserverApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Configures dynamic properties for the test environment.
     * <p>
     * This method uses {@link DynamicPropertySource} to override application properties at runtime. It sets up a local file-based Git repository for the config server and provides the necessary security properties required by {@link com.aspiresys.fp_micro_configserver.config.SecurityConfig}.
     * Using a random port (by setting {@code server.port=0}) prevents port conflicts during test execution.
     * </p>
     *
     * @param registry The registry for adding dynamic properties.
     */
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> 0); // random port
        registry.add("spring.cloud.config.server.git.uri", () -> "file://./config-repo");
        registry.add("spring.cloud.config.server.git.default-label", () -> "main");
        registry.add("app.allowed.origins", () -> "http://localhost:8080"); 
        registry.add("app.security.localhost.ipv4", () -> "127.0.0.1");
        registry.add("app.security.localhost.ipv6", () -> "0:0:0:0:0:0:0:1");
        registry.add("app.security.error.message", () -> "Only local requests are allowed for testing");
    }

    /**
     * Verifies that the Spring application context loads without errors.
     * A successful run of this test indicates that the application's configuration is valid and all beans are correctly initialized.
     */
    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    /**
     * Verifies that the Spring Cloud Config Server is enabled by checking for the presence of the {@link EnvironmentController} bean.
     * This confirms that the {@code @EnableConfigServer} annotation is effective.
     */
    @Test
    void configServerIsEnabled() {
        assertNotNull(applicationContext.getBean(EnvironmentController.class));
    }

    /**
     * Verifies that the application starts successfully on a random port and that the main application bean is present in the context.
     */
    @Test
    void applicationStartsSuccessfully() {
        assertTrue(port > 0, "Application should start with a valid port");
        assertNotNull(applicationContext.getBean(FpMicroConfigserverApplication.class));
    }
}
