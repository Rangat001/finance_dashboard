package com.example.finance.finance.config;

import com.example.finance.finance.security.JWTfilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity
 * Purpose:
 * - Defines route security rules (public vs protected)
 * - Enables stateless JWT security (no session)
 * - Registers JWTfilter
 * - Exposes PasswordEncoder bean for hashing passwords in DB
 */
@Configuration
@EnableWebSecurity
public class SpringSecurity {

    @Autowired
    JWTfilter jwtFilter;

    public SpringSecurity(JWTfilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * securityFilterChain()
     * Purpose:
     * - Disable CSRF for REST APIs
     * - Stateless session policy
     * - Add JWT filter before username/password filter
     * - Define endpoint access rules by role
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Example: admin-only endpoints
                        .requestMatchers("/Admin/**").hasRole("ADMIN")

                        // Example: analyst endpoints (and admin can also access)
                        .requestMatchers("/analyst/**")
                        .hasAnyRole("ANALYST", "ADMIN")

                        // Example: viewer/analyst/admin can read
                        .requestMatchers("/view/**")
                        .hasAnyRole("VIEWER", "ANALYST", "ADMIN")

                        // Everything else must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * passwordEncoder()
     * Purpose: Hashes user passwords before storing into DB and verifies on login.
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);   // use 10 for production
    }
}