# FP Micro Config Server

## Overview

The **FP Micro Config Server** is a centralized configuration service built with Spring Cloud Config Server. It serves as the configuration hub for all microservices in the FP (Final Project) ecosystem, providing externalized configuration management in a distributed system.

This service allows multiple microservices to retrieve their configuration properties from a single, centralized location, enabling easy configuration management across different environments (development, testing, production).

## Architecture

```text
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Auth Service  │    │  User Service   │    │ Product Service │
│                 │    │                 │    │                 │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          │               HTTP GET Requests              │
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │   Config Server (8888)   │
                    │                           │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │  Configuration Repository │
                    │   (Local/Git Repository)  │
                    └───────────────────────────┘
```

## Key Features

- **Centralized Configuration Management**: Single source of truth for all microservice configurations
- **Environment-Specific Configurations**: Support for different profiles (dev, test, prod)
- **Security**: Localhost-only access with CORS support for frontend applications
- **File-Based Repository**: Uses local file system or Git repository for configuration storage
- **Health Monitoring**: Spring Boot Actuator endpoints for monitoring
- **Logging Configuration**: Centralized logging configuration for all services

## Technology Stack

- **Spring Boot**: 3.3.12
- **Spring Cloud Config Server**: 2023.0.5
- **Spring Security**: For access control
- **Spring Boot Actuator**: For health monitoring
- **Java**: 17
- **Maven**: Build tool

## Configuration

### Core Application Properties

The main configuration is located in `src/main/resources/application.properties`:

```properties
# Server Configuration
spring.application.name=fp_micro_configserver
server.port=8888

# Git Repository Configuration (Local)
spring.cloud.config.server.git.uri=file:///C:/path/to/fp_files_configproperties
spring.cloud.config.server.git.search-paths=**

# Security Configuration
app.allowed.origins=http://localhost:8080,http://localhost:8081,http://localhost:8082
app.security.localhost.ipv4=127.0.0.1
app.security.localhost.ipv6=0:0:0:0:0:0:0:1
app.security.error.message=Only local requests are allowed
```

### Configuration Repository Structure

The configuration files are stored in the `fp_files_configproperties` directory:

```text
fp_files_configproperties/
├── application.properties          # Global configuration
├── fp_micro_authservice.properties # Auth service specific config
├── fp_micro_userservice.properties # User service specific config
├── fp_micro_gateway.properties     # Gateway specific config
├── fp_micro_orderservice.properties
├── fp_micro_productservice.properties
├── fp_micro_discoveryserver.properties
└── *.xml files                     # Logback configurations
```

### Important Configuration Settings

#### 1. Repository Configuration

```properties
# Local file system (Active)
spring.cloud.config.server.git.uri=file:///path/to/config/repo

# Alternative: Remote Git repository (Commented out)
# spring.cloud.config.server.git.uri=https://github.com/user/config-repo.git
# spring.cloud.config.server.git.username=username
# # spring.cloud.config.server.git.password=token
```

#### 2. Security Settings

```properties
# CORS allowed origins for frontend applications
app.allowed.origins=http://localhost:8080,http://localhost:8081,http://localhost:8082

# Localhost IP addresses for security filtering
app.security.localhost.ipv4=127.0.0.1
app.security.localhost.ipv6=0:0:0:0:0:0:0:1
```

## Key Components

### 1. Main Application Class

**File**: `src/main/java/com/aspiresys/fp_micro_configserver/FpMicroConfigserverApplication.java`

```java
@SpringBootApplication
@EnableConfigServer
public class FpMicroConfigserverApplication {
    public static void main(String[] args) {
        SpringApplication.run(FpMicroConfigserverApplication.class, args);
    }
}
```

The `@EnableConfigServer` annotation enables Spring Cloud Config Server functionality.

### 2. Security Configuration

**File**: `src/main/java/com/aspiresys/fp_micro_configserver/config/SecurityConfig.java`

This class implements:

- **CORS Policy**: Allows cross-origin requests from configured frontend applications
- **Localhost-Only Access**: Restricts access to local requests only (127.0.0.1 and ::1)
- **Security Filter Chain**: Disables CSRF and permits all authenticated requests

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                .anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter corsAndLocalhostFilter() {
        // Custom filter implementation for CORS and localhost restriction
    }
}
```

### 3. Constants Definition

**File**: `src/main/java/com/aspiresys/fp_micro_configserver/config/ConfigServerConstants.java`

Contains all the constants used throughout the application:

- CORS header names and values
- HTTP patterns
- Delimiter configurations

### 4. Servlet Initializer

**File**: `src/main/java/com/aspiresys/fp_micro_configserver/ServletInitializer.java`

Enables deployment as a WAR file in external servlet containers.

## API Endpoints

The Config Server exposes several REST endpoints for configuration retrieval:

### Configuration Endpoints

| HTTP Method | Endpoint Pattern                      | Description                                             |
| ----------- | ------------------------------------- | ------------------------------------------------------- |
| GET         | `/{application}/{profile}`            | Get configuration for application with specific profile |
| GET         | `/{application}/{profile}/{label}`    | Get configuration with specific Git label/branch        |
| GET         | `/{application}-{profile}.properties` | Get properties file format                              |
| GET         | `/{application}-{profile}.yml`        | Get YAML file format                                    |

### Example Requests

```bash
# Get auth service configuration for default profile
GET http://localhost:8888/fp_micro_authservice/default

# Get user service configuration for production profile
GET http://localhost:8888/fp_micro_userservice/prod

# Get configuration in properties format
GET http://localhost:8888/fp_micro_gateway-default.properties

# Get configuration in YAML format
GET http://localhost:8888/fp_micro_orderservice-dev.yml
```

### Health Check Endpoints

```bash
# Application health
GET http://localhost:8888/actuator/health

# Config server specific health
GET http://localhost:8888/actuator/configserver
```

## Running the Application

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Configuration repository set up in `fp_files_configproperties`

### Development Mode

```bash
# Clone the repository
git clone <repository-url>
cd fp_micro_configserver

# Run with Maven
./mvnw spring-boot:run

# Or run the JAR file
./mvnw clean package
java -jar target/fp_micro_configserver-0.0.1-SNAPSHOT.war
```

## Configuration Management

### Adding New Service Configuration

1. Create a new properties file in `fp_files_configproperties/`:

   ```bash
   touch fp_files_configproperties/fp_micro_newservice.properties
   ```

2. Add service-specific configuration:

   ```properties
   # fp_micro_newservice.properties
   server.port=8090
   spring.datasource.url=jdbc:h2:mem:newservice
   logging.level.com.aspiresys=DEBUG
   ```

3. The new configuration will be automatically available at:

   ```bash
   http://localhost:8888/fp_micro_newservice/default
   ```

### Environment-Specific Configuration

Create profile-specific files:

```mermaid
fp_files_configproperties/
├── fp_micro_userservice.properties      # Default profile
├── fp_micro_userservice-dev.properties  # Development profile
├── fp_micro_userservice-test.properties # Test profile
└── fp_micro_userservice-prod.properties # Production profile
```

### Centralized Logging Configuration

All services can use centralized logging configuration through `logback-spring.xml` files:

```xml
<!-- Example: fp_micro_authservice-logback-spring.xml -->
<configuration>
    <property name="LOG_PATH" value="./logs/auth-service"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n"/>

    <appender name="FILE_ALL" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/auth-service.log</file>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/archived/auth-service.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
</configuration>
```

## Security Considerations

### Access Control

- **Localhost Only**: The service only accepts requests from localhost (127.0.0.1 and ::1)
- **CORS Configuration**: Specific origins are whitelisted for frontend access
- **No Authentication**: Currently configured for development environment

### Production Security Recommendations

1. **Enable Authentication**:

   ```properties
   spring.security.user.name=configuser
   spring.security.user.password=securepassword
   ```

2. **Use HTTPS**:

   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=password
   ```

3. **Encrypt Sensitive Properties**:

   ```bash
   # Use Spring Cloud Config encryption
   curl localhost:8888/encrypt -d mysecret
   ```

## Monitoring and Troubleshooting

### Health Monitoring

The service includes Spring Boot Actuator endpoints:

```bash
# General health
curl http://localhost:8888/actuator/health

# Config server specific metrics
curl http://localhost:8888/actuator/configserver

# Environment variables
curl http://localhost:8888/actuator/env
```

### Log Files

Logs are written to:

```mermaid
logs/
├── application.log         # General application logs
├── application-error.log   # Error logs
└── archived/              # Archived log files
```

### Common Issues

1. **Configuration Not Found**: Check if the configuration file exists in the repository
2. **Access Denied**: Verify that requests are coming from localhost
3. **CORS Issues**: Check the `app.allowed.origins` configuration

### Testing

Run the test suite:

```bash
./mvnw test
```

Key test classes:

- `FpMicroConfigserverApplicationTests`: Integration tests
- `SecurityConfigTest`: Security configuration tests

## Integration with Other Services

### Client Configuration

Other microservices connect to this config server by adding to their `application.properties`:

```properties
spring.application.name=fp_micro_userservice
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.fail-fast=true
spring.cloud.config.retry.initial-interval=3000
spring.cloud.config.retry.max-interval=4000
spring.cloud.config.retry.max-attempts=20
```

### Service Discovery Integration

When used with Eureka Discovery Server:

```properties
# In client services
spring.cloud.config.discovery.enabled=true
spring.cloud.config.discovery.service-id=fp_micro_configserver
```

## Best Practices

1. **Version Control**: Keep configuration files in Git for version control
2. **Environment Separation**: Use profiles for different environments
3. **Sensitive Data**: Encrypt sensitive configuration values
4. **Monitoring**: Monitor configuration changes and access patterns
5. **Backup**: Regularly backup configuration repository
6. **Documentation**: Document configuration properties and their purposes

## Contributing

1. Follow the existing code structure and naming conventions
2. Add tests for new functionality
3. Update documentation for configuration changes
4. Ensure security best practices are maintained

## References

- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [Spring Boot Actuator Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
