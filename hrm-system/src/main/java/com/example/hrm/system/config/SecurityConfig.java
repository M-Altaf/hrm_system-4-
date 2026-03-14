package com.example.hrm.system.config;


import com.example.hrm.system.security.CustomUserDetailsService;
import com.example.hrm.system.security.JwtAuthenticationFilter;
import com.example.hrm.system.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(userDetailsService, jwtUtil);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ── PUBLIC ──────────────────────────────────────────────
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ── EMPLOYEE ENDPOINTS ───────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/employees/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/employees/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/employees/**").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/employees/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── LEAVE ENDPOINTS ──────────────────────────────────────
                        // Apply for leave — any employee
                        .requestMatchers(HttpMethod.POST,
                                "/api/leaves").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        // View all leaves — ADMIN and HR only
                        .requestMatchers(HttpMethod.GET,
                                "/api/leaves").hasAnyRole("ADMIN", "HR")
                        // View pending leaves — ADMIN, HR, MANAGER
                        .requestMatchers(HttpMethod.GET,
                                "/api/leaves/pending").hasAnyRole("ADMIN", "HR", "MANAGER")
                        // View single leave — all roles
                        .requestMatchers(HttpMethod.GET,
                                "/api/leaves/{id}").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        // View leaves by employee — all roles
                        .requestMatchers(HttpMethod.GET,
                                "/api/leaves/employee/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        // Approve or reject — ADMIN, HR, MANAGER only
                        .requestMatchers(HttpMethod.PUT,
                                "/api/leaves/*/status").hasAnyRole("ADMIN", "HR", "MANAGER")
                        // Cancel leave — any employee
                        .requestMatchers(HttpMethod.PUT,
                                "/api/leaves/*/cancel").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── DEPARTMENT ENDPOINTS ─────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/departments/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── DESIGNATION ENDPOINTS ────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/designations/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── CATEGORY ENDPOINTS ───────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── PAYROLL ENDPOINTS ────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/payrolls/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/payrolls/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/payrolls/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/payrolls/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // ── ATTENDANCE ENDPOINTS ─────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/attendance/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── NOTIFICATION ENDPOINTS ───────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/notifications/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET,
                                "/api/notifications/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/notifications/**").hasAnyRole("ADMIN", "HR")

                        // ── AUDIT LOG ENDPOINTS ──────────────────────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/audit-logs/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(
                jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}