package com.tang0488;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 返回不进行密码编码的编码器
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity, enable it in production
                .authorizeHttpRequests(authorize -> authorize
//                                .requestMatchers("/").permitAll() // Allow public access to these endpoints
//                                .requestMatchers("/game/register").permitAll() // Allow public access to register endpoint
//                                .requestMatchers("/game/**").authenticated() // Require authentication for /game/** endpoints
                                .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1) // Ensures only one session per user
                        .maxSessionsPreventsLogin(true)
                );

        return http.build();
    }
}
