package com.example.beach_safety_water_quality.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/beach/zones/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/beach/zones/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/beach/zones/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/beach/zones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/beach/samples/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/beach/samples/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/beach/samples/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/beach/samples/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/contacts/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/contacts/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/contacts/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/contacts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/ledger/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/ledger/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/ledger/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/ledger/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/beach/monitoring/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                .requestMatchers(HttpMethod.GET, "/api/beach/dashboard/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                .requestMatchers(HttpMethod.GET, "/api/beach/reports/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}