package com.zoonza.pay.bootstrap.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    private static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(
            pathPattern(POST, "/api/customers/signup"),
            pathPattern(POST, "/api/verifications"),
            pathPattern(POST, "/api/verifications/{verificationId}/confirm"),
            pathPattern(POST, "/api/auth/login"),
            pathPattern(POST, "/api/auth/reissue"),
            pathPattern(POST, "/api/auth/logout"),
            pathPattern("/error")
    );

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ProblemDetailAuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .bearerTokenResolver(bearerTokenResolver())
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }

    private BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();

        return request ->
                PUBLIC_ENDPOINTS.matches(request)
                        ? null
                        : delegate.resolve(request);
    }
}
