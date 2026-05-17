package com.freightos.admin.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class AdminUserDetailsService {

    @Bean
    public InMemoryUserDetailsManager adminUserDetailsManager(
            AdminUserProperties properties,
            PasswordEncoder passwordEncoder) {

        UserDetails[] users = properties.getUsers().stream()
                .map(entry -> User.withUsername(entry.getUsername())
                        .password(passwordEncoder.encode(entry.getPassword()))
                        .roles(entry.getRoles().toArray(String[]::new))
                        .build())
                .toArray(UserDetails[]::new);

        return new InMemoryUserDetailsManager(users);
    }
}
