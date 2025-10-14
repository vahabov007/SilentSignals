package com.vahabvahabov.SilentSignals.security;

import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.repository.UserRepository;
import com.vahabvahabov.SilentSignals.security.jwt.JwtRequestFilter;
import com.vahabvahabov.SilentSignals.security.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Optional;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(UserRepository userRepository, CorsConfigurationSource corsConfigurationSource) {
        this.userRepository = userRepository;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return usernameOrMail -> {
            Optional<User> userOptional = userRepository.findByMail(usernameOrMail);

            if (userOptional.isEmpty()) {
                userOptional = userRepository.findByUsername(usernameOrMail);
            }
            if(userOptional.isEmpty()) {
                throw new UsernameNotFoundException("Username not found.");
            }
            return userOptional.get();

        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        JwtRequestFilter filter = new JwtRequestFilter();
        filter.setUserDetailsService(userDetailsService);
        filter.setJwtUtil(jwtUtil);
        return filter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/style.css",
                                "/script.js",
                                "/forgot-password.js",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/my-login",
                                "/register",
                                "/forgot-password",
                                "/favicon.ico",
                                "/ws/**",
                                "/api/auth/**",
                                "/api/register/**",
                                "/home",
                                "/",
                                "home.css",
                                "home.js"
                        ).permitAll()
                        .requestMatchers("/trusted/api/**", "/api/alert/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}