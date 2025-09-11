package com.irrigation.erp.backend.config;

import com.irrigation.erp.backend.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API
                .cors(cors -> cors.configurationSource(securityCorsConfigurationSource())) // Enable CORS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions, use JWT
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints - no authentication required
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/roles").permitAll()
                        .requestMatchers("/api/auth/check-password-change/**").permitAll()
                        .requestMatchers("/api/auth/validate-token").permitAll()
                        .requestMatchers("/api/requests/material-distribution").permitAll()
//                        .requestMatchers("/api/grn/**").hasAnyRole("STOCK_KEEPER","ADMIN")


                        // Protected endpoints - require authentication
                        .anyRequest().authenticated()
                )
                // Add JWT filter before Spring Security's authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource securityCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (update with your frontend URLs)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",  // React default
                "http://localhost:5173",  // Vite default
                "http://localhost:4200"   // Angular default

        ));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

//
//package com.irrigation.erp.backend.config;
//
//import com.irrigation.erp.backend.filter.JwtAuthenticationFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity // optional, but handy if you want @PreAuthorize later
//public class SecurityConfig {
//
//    @Autowired
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // Stateless JWT API
//                .csrf(AbstractHttpConfigurer::disable)
//                // Use the CORS bean defined in CorsConfig (no CORS bean here)
//                .cors(Customizer.withDefaults())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                .authorizeHttpRequests(authorize -> authorize
//                        // Preflight
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                        // Public auth endpoints
//                        .requestMatchers("/api/auth/login").permitAll()
//                        .requestMatchers("/api/auth/logout").permitAll()
//                        .requestMatchers("/api/auth/roles").permitAll()
//                        .requestMatchers("/api/auth/check-password-change/**").permitAll()
//                        .requestMatchers("/api/auth/validate-token").permitAll()
//
//                        // Keep public if intended
//                        .requestMatchers("/api/requests/material-distribution").permitAll()
//
//                        // GRN endpoints (adjust roles to match your JWT authorities mapping)
//                        .requestMatchers(HttpMethod.POST, "/api/grn/**")
//                        .hasAnyRole("STOCK_KEEPER", "ADMIN")
//                        .requestMatchers("/api/grn/**")
//                        .hasAnyRole("STOCK_KEEPER", "ADMIN")
//
//                        // Everything else requires auth
//                        .anyRequest().authenticated()
//                )
//
//                // JWT filter
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
