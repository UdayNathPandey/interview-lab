package com.interviewlab.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
            throws Exception
    {
        return httpSecurity
                .authorizeHttpRequests(
                        authorizationManagerRequestMatcherRegistry ->
                                authorizationManagerRequestMatcherRegistry
                                        .requestMatchers(   "/api/public/**","/auth/test").permitAll()
//                                        .requestMatchers("/auth/test").permitAll()
                                        .requestMatchers("/auth/login").permitAll()
                                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()) // header me username password send krne k liye
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable()) // iske bina security chal nhi rhi h
                .build();
    }

//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder)
//    {
//        UserDetails user = User
//                .withUsername("Uday")
//                .password(passwordEncoder.encode("password"))
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(user);
//
//    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // to get encoded value
//    @Bean
//    CommandLineRunner passwordTest(
//            PasswordEncoder passwordEncoder) {
//
//        return args -> {
//            System.out.println(
//                    passwordEncoder.encode("password")
//            );
//        };
//    }

    // for learning/inspection purpose -> it is by default set up by spring ,not really need to be added here
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception
    {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
