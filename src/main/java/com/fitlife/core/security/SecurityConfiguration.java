package com.fitlife.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ENDPOINTS (AC1: Đã pass permitAll)
                        .requestMatchers("/auth/**", "/api/v1/auth/**").permitAll()
                        .requestMatchers("/ai/**", "/api/v1/ai/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/packages/**", "/api/v1/packages/**").permitAll()
                        .requestMatchers("/payment/vnpay-return", "/api/v1/payment/vnpay-return", "/payment/vnpay-ipn", "/api/v1/payment/vnpay-ipn").permitAll()

                        // 2. BUSINESS & HEALTH (Chấp nhận cả có ROLE_ và không có ROLE_)
                        .requestMatchers("/workout/**", "/api/v1/workout/**").hasAnyAuthority("MEMBER", "ROLE_MEMBER", "STAFF", "ROLE_STAFF", "ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/health/**", "/api/v1/health/**").hasAnyAuthority("MEMBER", "ROLE_MEMBER", "STAFF", "ROLE_STAFF", "ADMIN", "ROLE_ADMIN")

                        // Riêng /members/me thì ai đăng nhập vào cũng xem được hồ sơ của mình
                        .requestMatchers(HttpMethod.GET, "/members/me", "/api/v1/members/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/members/me", "/api/v1/members/me").authenticated()

                        // Các API members khác (như /members/admin) thì vẫn siết quyền
                        .requestMatchers("/members/**", "/api/v1/members/**").hasAnyAuthority("MEMBER", "ROLE_MEMBER", "STAFF", "ROLE_STAFF", "ADMIN", "ROLE_ADMIN")

                        .requestMatchers("/payment/**", "/api/v1/payment/**").hasAnyAuthority("MEMBER", "ROLE_MEMBER")

                        // 3. ADMINISTRATION
                        .requestMatchers(HttpMethod.POST, "/packages/**", "/api/v1/packages/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "STAFF", "ROLE_STAFF")

                        // 4. CHECK-IN ROUTE
                        .requestMatchers("/checkin/**", "/api/v1/checkin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "STAFF", "ROLE_STAFF", "MEMBER", "ROLE_MEMBER")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}