package kongju.pickmeal.support.fixture;

import kongju.pickmeal.api.security.CustomAccessDeniedHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableMethodSecurity // PreAuthorize사용
public class TestSecurityConfig {
    @Bean
    SecurityFilterChain testSecurityFilterChain(
            HttpSecurity http,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) throws Exception {
        return http
                // csrf끄기
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // 인증 정보는 확인
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .build();
    }
}
