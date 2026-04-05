package com.brandon.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Configuração de segurança HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ❌ desabilita CSRF (API REST não precisa)
            .csrf(csrf -> csrf.disable())

            // 🔓 define regras de acesso
            .authorizeHttpRequests(auth -> auth

                // rotas públicas
                .requestMatchers("/users/register").permitAll()

                // qualquer outra rota precisa de autenticação
                .anyRequest().authenticated()
            )

            // ⚠️ por enquanto sem login (JWT virá depois)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
