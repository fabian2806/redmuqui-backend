package com.redmuqui.platform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@ConditionalOnProperty(name = "app.security.method-enabled", havingValue = "true", matchIfMissing = true)
@EnableMethodSecurity
public class MethodSecurityConfig {
}
