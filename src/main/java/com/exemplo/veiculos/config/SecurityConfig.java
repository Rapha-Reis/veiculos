package com.exemplo.veiculos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // Configuração de segurança comum para ambos os modos
        http.csrf(csrf -> csrf.disable());

        /* 
         * =========================================================================
         * MODO 1: PROTEGIDO COM AUTENTICAÇÃO JWT (PADRÃO DE PRODUÇÃO)
         * =========================================================================
         * PARA TESTAR SEM TOKEN NO SWAGGER: Comente todo este bloco (Modo 1) 
         * e descomente o bloco de baixo (Modo 2).
         */
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/veiculos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/veiculos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/veiculos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/veiculos").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        /* ========================================================================= */


        /* 
         * =========================================================================
         * MODO 2: LIBERADO PARA TESTES LOCAIS (SEM NECESSIDADE DE TOKEN / JWT)
         * =========================================================================
         * PARA VOLTAR AO MODO SEGURO: Comente todo este bloco (Modo 2)
         * e descomente o bloco de cima (Modo 1).
         *
          http
              .authorizeHttpRequests(auth -> auth
                  .anyRequest().permitAll()
              );
         * ========================================================================= 
         */

        return http.build();
    }
}