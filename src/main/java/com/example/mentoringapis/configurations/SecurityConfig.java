package com.example.mentoringapis.configurations;

import com.example.mentoringapis.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig{

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(){
        return new JwtAuthenticationFilter();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/sign-in/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/sign-up/**").permitAll()
                        .requestMatchers("/sign-up/email-verification").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf().disable()
                .addFilterBefore(jwtAuthenticationFilter(), LogoutFilter.class);
        return http.build();
    }
}
