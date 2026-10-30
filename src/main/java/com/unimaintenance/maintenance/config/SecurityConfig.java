
package com.unimaintenance.maintenance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${DEMO_STUDENT_PASSWORD:}") String studentPassword,
            @Value("${DEMO_ADMIN_PASSWORD:}") String adminPassword) {

        if (studentPassword.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException(
                "Set DEMO_STUDENT_PASSWORD and DEMO_ADMIN_PASSWORD");
        }

        UserDetails student = User.withUsername("student1")
                .password(encoder.encode(studentPassword))
                .roles("STUDENT")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(student, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/", "/login", "/css/**", "/error")
                    .permitAll()

                        
                .requestMatchers(
                    HttpMethod.GET,
                    "/complaints/new",
                    "/complaints/submitted",
                    "/complaints/mine")
                    .hasAnyRole("STUDENT", "ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/complaints/save")
                    .hasAnyRole("STUDENT", "ADMIN")

                .requestMatchers("/complaints", "/complaints/**")
                    .hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
