package com.example.hrm.system.config;

import com.example.hrm.system.security.CustomUserDetailsService;
import com.example.hrm.system.security.JwtAuthenticationFilter;
import com.example.hrm.system.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil            = jwtUtil;
    }

    // ✅ Manually create filter — prevents double execution
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    // ✅ Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ FIX 1 — @Bean annotation REMOVED to fix AuthenticationProvider conflict warning.
    //    Method is still used internally by securityFilterChain below.
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ✅ AuthenticationManager for login
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .cors(cors -> cors.configure(http))   // ✅ CORS line add ki
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth


                        // ── AUTH (specific rule BEFORE the wildcard) ─────────
                        .requestMatchers(HttpMethod.GET, "/api/auth/users").hasRole("ADMIN")
                        // ── PUBLIC ────────────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // ── EMPLOYEE ──────────────────────────────────────────
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

                        // ── LEAVE ─────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/leaves/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET,
                                "/api/leaves/pending").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .requestMatchers(HttpMethod.GET,
                                "/api/leaves/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        // ✅ FIX 2 — EMPLOYEE removed from PUT (approve/reject should not be done by EMPLOYEE)
                        .requestMatchers(HttpMethod.PUT,
                                "/api/leaves/**").hasAnyRole("ADMIN", "HR", "MANAGER")

                        // ── DEPARTMENT ────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/departments/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── DESIGNATION ───────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/designations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/designations/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── CATEGORY ──────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── PAYROLL ───────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/payrolls/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/payrolls/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/payrolls/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/payrolls/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // ── ATTENDANCE ────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/attendance/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/attendance/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                        // ── NOTIFICATION ──────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/notifications/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.GET,
                                "/api/notifications/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/notifications/**").hasAnyRole("ADMIN", "HR")

                        // ── AUDIT LOG ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/audit-logs/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}