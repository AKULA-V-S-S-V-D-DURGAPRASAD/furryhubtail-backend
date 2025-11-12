package com.furryhub.petservices.config;


import com.furryhub.petservices.service.CustomUserDetailsService;
import com.furryhub.petservices.util.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Other imports...
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Additional imports
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

import static com.furryhub.petservices.model.entity.Role.ADMIN;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(corsCustomizer -> corsCustomizer
                .configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    // "http://localhost:8081" add
                    config.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:8080", "https://your-production-domain.com"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                })
            )
            .csrf(csrfCustomizer -> csrfCustomizer.disable()) 
            .authorizeHttpRequests(authCustomizer -> authCustomizer
                .requestMatchers(
                    "/api/auth/**",
                    "/api/auth/register/customer",
                    "/api/auth/register/provider",
                    "/api/auth/verify-email",
                        "/api/geocode",
                        "/api/packages/**",
                        "/api/package/**"
                ).permitAll()
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
              .requestMatchers("/api/provider/**").hasAnyRole("PROVIDER","ADMIN")
                    .requestMatchers("api/cart/**").hasRole("CUSTOMER")
               .anyRequest().authenticated()
            )
            .sessionManagement(sessionCustomizer -> sessionCustomizer
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptionCustomizer -> exceptionCustomizer
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                })
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
     AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
     AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
     PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}