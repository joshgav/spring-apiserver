package com.joshgav.apiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "security.oauth2.client.registration.oidc", name = "provider")
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .oauth2Client(withDefaults())
          .securityMatchers((matchers) -> matchers
            .requestMatchers("/auth/**"))
          .authorizeRequests()
            .anyRequest().authenticated()
            .and().oauth2Login()
            .and().logout().logoutSuccessUrl("/_logout");

        http.csrf().disable();
        return http.build();
    }
}