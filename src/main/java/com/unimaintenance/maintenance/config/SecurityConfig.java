
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

            @Value("${DEMO_STUDENT_PASSWORD:}")
            String studentPassword,

            @Value("${DEMO_ADMIN_PASSWORD:}")
            String adminPassword,

            @Value("${DEMO_LECTURER_PASSWORD:}")
            String lecturerPassword,

            @Value("${DEMO_STAFF_PASSWORD:}")
            String staffPassword) {

        // Existing accounts are required.

        if (studentPassword.isBlank() ||
                adminPassword.isBlank()) {

            throw new IllegalStateException(
                "Please configure DEMO_STUDENT_PASSWORD " +
                "and DEMO_ADMIN_PASSWORD."
            );
        }

        // Existing student account.

        UserDetails student = User.withUsername("student1")
                .password(encoder.encode(studentPassword))
                .roles("STUDENT")
                .build();

        // Existing administrator account.

        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        InMemoryUserDetailsManager users =
                new InMemoryUserDetailsManager(student, admin);

        // Lecturer account.
        // Created only when a password is configured.

        if (lecturerPassword != null &&
                !lecturerPassword.isBlank()) {

            UserDetails lecturer =
                    User.withUsername("lecturer1")
                            .password(
                                encoder.encode(lecturerPassword)
                            )
                            .roles("LECTURER")
                            .build();

            users.createUser(lecturer);
        }

        // Non-academic staff account.
        // Created only when a password is configured.

        if (staffPassword != null &&
                !staffPassword.isBlank()) {

            UserDetails staff =
                    User.withUsername("staff1")
                            .password(
                                encoder.encode(staffPassword)
                            )
                            .roles("STAFF")
                            .build();

            users.createUser(staff);
        }

        return users;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // Public website pages and assets.

                .requestMatchers(
                    "/",
                    "/login",
                    "/css/**",
                    "/images/**",
                    "/error"
                ).permitAll()

                // Personal complaint editing pages.
                // Server-side ownership checks still apply.

                .requestMatchers(
                    HttpMethod.GET,
                    "/complaints/my/edit/*",
                    "/complaints/my/photo/*"
                ).hasAnyRole(
                    "STUDENT",
                    "LECTURER",
                    "STAFF"
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/complaints/my/update/*"
                ).hasAnyRole(
                    "STUDENT",
                    "LECTURER",
                    "STAFF"
                )

                // Complaint submission and tracking.

                .requestMatchers(
                    HttpMethod.GET,
                    "/complaints/new",
                    "/complaints/submitted"
                ).hasAnyRole(
                    "STUDENT",
                    "LECTURER",
                    "STAFF",
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.GET,
                    "/complaints/mine"
                ).hasAnyRole(
                    "STUDENT",
                    "LECTURER",
                    "STAFF"
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/complaints/save"
                ).hasAnyRole(
                    "STUDENT",
                    "LECTURER",
                    "STAFF",
                    "ADMIN"
                )

                // All remaining complaint management routes
                // are restricted to administrators.

                .requestMatchers(
                    "/complaints",
                    "/complaints/**"
                ).hasRole("ADMIN")

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
