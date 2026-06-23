package com.banque.abc.tpe.config;

import com.banque.abc.tpe.security.CustomUserDetailsService;
import com.banque.abc.tpe.security.JwtAuthenticationEntryPoint;
import com.banque.abc.tpe.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(parseAllowedOrigins(allowedOrigins));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws-notifications", "/ws-notifications/**").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/dashboard/**").hasAnyRole("MONETIQUE", "AGENCE", "ADMIN")
                        .requestMatchers("/api/tpe/**", "/api/tpes/**").hasAnyRole("MONETIQUE", "AGENCE", "ADMIN")
                        .requestMatchers("/api/commercant/**", "/api/commercants/**").hasAnyRole("MONETIQUE", "AGENCE", "ADMIN")
                        .requestMatchers("/api/demande/**", "/api/demandes/**").hasAnyRole("MONETIQUE", "AGENCE", "ADMIN")
                        .requestMatchers("/api/affectation/**", "/api/affectations/**").hasAnyRole("MONETIQUE", "ADMIN")
                        .requestMatchers("/api/panne/**", "/api/pannes/**").hasAnyRole("MONETIQUE", "AGENCE", "ADMIN")
                        .requestMatchers("/api/assistant/**", "/api/assistant-metier/**", "/api/assistant-ia/**")
                        .hasAnyRole("MONETIQUE", "AGENCE", "ADMIN", "MANAGER", "AGENT")
                        .requestMatchers("/api/notifications-ia/**").hasAnyRole("MONETIQUE", "AGENCE", "ADMIN")
                        .requestMatchers("/api/notifications/**").hasAnyRole("MONETIQUE", "AGENCE", "ADMIN")
                        .requestMatchers("/api/taux/**").hasAnyRole("MONETIQUE", "ADMIN")
                        .requestMatchers("/api/audit/**", "/api/audit-logs/**").hasAnyRole("MONETIQUE", "ADMIN")
                        .requestMatchers("/api/fichier-bancaire/**").hasAnyRole("MONETIQUE", "ADMIN")
                        .requestMatchers("/api/tpe-posting/**").hasAnyRole("MONETIQUE", "ADMIN")
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private List<String> parseAllowedOrigins(String allowedOrigins) {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .filter(origin -> !"*".equals(origin))
                .collect(Collectors.toList());
    }
}
