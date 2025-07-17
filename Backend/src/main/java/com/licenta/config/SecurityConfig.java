package com.licenta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configurație pentru securitatea aplicației.
 * Definește un bean pentru criptarea parolelor utilizând algoritmul BCrypt.
 */
@Configuration
public class SecurityConfig {

    /**
     * Creează un bean de tip BCryptPasswordEncoder pentru criptarea și verificarea parolelor.
     *
     * @return un obiect BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
