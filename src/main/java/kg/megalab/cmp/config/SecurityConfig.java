package kg.megalab.cmp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakRoleConverter keycloakRoleConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/doctor/all",
                                "/api/v1/specialty/all",
                                "/api/v1/lab-test/all",
                                "/api/v1/lab-slot/available",
                                "/api/v1/slot/available"
                        ).hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/appointment/patient/*",
                                "/api/v1/lab-appointment/patient/*"
                        ).hasAnyRole("PATIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/appointment/doctor/*"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/patient/all",
                                "/api/v1/appointment/all"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/appointment/filter"
                        ).hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/doctor/create",
                                "/api/v1/patient/create",
                                "/api/v1/specialty/create",
                                "/api/v1/lab-test/create",
                                "/api/v1/lab-slot/create"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/appointment/book",
                                "/api/v1/lab-appointment/book"
                        ).hasAnyRole("PATIENT", "ADMIN")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/schedule/create"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/schedule/*/update"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/schedule/all"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/appointment/*/complete",
                                "/api/v1/lab-appointment/*/complete"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/appointment/*/cancel"
                        ).hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/lab-appointment/*/cancel"
                        ).hasAnyRole("PATIENT", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/slot/*"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/lab-request/send"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(keycloakRoleConverter))
                );
        return http.build();
    }

}