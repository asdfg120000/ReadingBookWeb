package com.example.reading.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtKey(
            @Value("${app.jwt.secret}") String secret
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET은 최소 32바이트여야 합니다."
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("reading-api")
        );

        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //확인용 html
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/reading-demo.html",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/signup",
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/works",
                                "/api/works/*",
                                "/api/public/records",
                                "/api/public/records/*"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(401);
                            response.setContentType(
                                    "application/json;charset=UTF-8"
                            );

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of(
                                            "status", 401,
                                            "message", "로그인이 필요합니다."
                                    )
                            );
                        })

                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(403);
                            response.setContentType(
                                    "application/json;charset=UTF-8"
                            );

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of(
                                            "status", 403,
                                            "message", "접근 권한이 없습니다."
                                    )
                            );
                        })
                )

                .oauth2ResourceServer(resource -> resource
                        .jwt(jwt -> {})
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(401);
                            response.setContentType(
                                    "application/json;charset=UTF-8"
                            );

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of(
                                            "status", 401,
                                            "message",
                                            "토큰이 유효하지 않거나 만료되었습니다."
                                    )
                            );
                        })
                );

        return http.build();
    }
}