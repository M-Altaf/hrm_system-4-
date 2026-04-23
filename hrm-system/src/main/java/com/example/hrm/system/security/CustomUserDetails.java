package com.example.hrm.system.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long   userId;
    private final String username;
    private final String password;
    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long userId, String username,
                             String password, String email, String role) {
        this.userId      = userId;
        this.username    = username;
        this.password    = password;
        this.email       = email;
        this.role        = role;
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)); // ✅ ROLE_ prefix
    }

    // ── Custom getters ────────────────────────────────────────────────
    public Long   getUserId() { return userId;   }
    public String getEmail()  { return email;    }
    public String getRole()   { return role;     }

    // ── UserDetails ───────────────────────────────────────────────────
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority>
    getAuthorities()       { return authorities; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}