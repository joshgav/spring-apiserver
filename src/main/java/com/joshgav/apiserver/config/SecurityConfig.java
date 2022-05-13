package com.joshgav.apiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
            .anyRequest().authenticated()
            .and().oauth2Login()
            .and().logout().logoutSuccessUrl("/_logout");

        http.csrf().disable();
        return http.build();
    }
}