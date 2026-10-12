package kongju.pickmeal.infrastructure.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import kongju.pickmeal.api.security.JwtTokenFilter;
import kongju.pickmeal.api.security.JwtAuthenticationService;
import kongju.pickmeal.api.security.CustomAccessDeniedHandler;
import kongju.pickmeal.api.security.CustomAuthenticationEntryPoint;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 작동을 위해
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationService jwtAuthenticationService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF설정 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // 스프링 시큐리티가 세션 생성X, 기존 세션 사용 X
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/users/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api-docs",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // UsernamePasswordAuthenticationFilter전에 jwt먼저 실행
                .addFilterBefore(new JwtTokenFilter(jwtAuthenticationService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
