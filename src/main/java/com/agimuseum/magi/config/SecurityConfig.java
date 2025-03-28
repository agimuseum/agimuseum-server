package com.agimuseum.magi.config;

import com.agimuseum.magi.exception.SecurityExceptionHandler;
import com.agimuseum.magi.filter.JwtAuthenticationFilter;
import com.agimuseum.magi.service.UserDetailsServiceImp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final UserDetailsServiceImp userDetailsServiceImp;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(UserDetailsServiceImp userDetailsServiceImp,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          SecurityExceptionHandler securityExceptionHandler) {
        this.userDetailsServiceImp = userDetailsServiceImp;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityExceptionHandler = securityExceptionHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure CSRF
        http = http.csrf(AbstractHttpConfigurer::disable);

        // Configure CORS
        http = http.cors(corsConfigurer ->
                corsConfigurer.configurationSource(corsConfigurationSource())
        );

        // Configure authorization rules
        http = http.authorizeHttpRequests(authConfig -> {
            // Public endpoints
            authConfig.requestMatchers("/register", "/login", "/forgotPassword/**").permitAll();
            authConfig.requestMatchers("/api/debug/**").permitAll();

            // OPTIONS requests for CORS
            authConfig.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

            // Public GET endpoints for photos - allow viewing photos without authentication
            authConfig.requestMatchers(HttpMethod.GET, "/api/photos/locations/**").permitAll();
            authConfig.requestMatchers(HttpMethod.GET, "/api/photos/stops/**").permitAll();

            // Specific public GET endpoints
            authConfig.requestMatchers(HttpMethod.GET, "/api/visits/rewards/progress").permitAll();
            authConfig.requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll();
            authConfig.requestMatchers(HttpMethod.GET, "/api/stops/**").permitAll();

            // API docs if available
            authConfig.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();

            // Protected endpoints
            authConfig.requestMatchers("/api/visits/summary").authenticated();
            authConfig.requestMatchers("/api/visits/**").authenticated();
            authConfig.requestMatchers("/api/users/**").authenticated();
            authConfig.requestMatchers(HttpMethod.POST, "/api/photos/**").authenticated();
            authConfig.requestMatchers(HttpMethod.DELETE, "/api/photos/**").authenticated();
            authConfig.requestMatchers("/api/profile/**").authenticated();
            authConfig.requestMatchers("/api/account/**").authenticated();
            authConfig.requestMatchers("/api/auth/**").authenticated();

            // All other requests
            authConfig.anyRequest().authenticated();
        });

        // Configure exception handling
        http = http.exceptionHandling(exConfig -> {
            exConfig.authenticationEntryPoint(securityExceptionHandler);
            exConfig.accessDeniedHandler(securityExceptionHandler);
        });

        // Set user details service
        http = http.userDetailsService(userDetailsServiceImp);

        // Configure session management
        http = http.sessionManagement(sessionConfig ->
                sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // Add JWT filter
        http = http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}