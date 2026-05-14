package kongju.pickmeal.api.auth;

import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.application.auth.data.AuthDto;
import kongju.pickmeal.common.exception.BusinessException;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RedisTemplate<String, String> restTemplate;

    @Nested
    @DisplayName("로그인 테스트")
    class Login {
        @Test
        @DisplayName("아이디와 일치하는 유저를 찾지 못했거나 비밀번호가 일치하지 않을 경우 에러 처리")
        public void should_fail_loginId_not_found() throws Exception {
            AuthDto.LoginRequest request = AuthDto.LoginRequest
                    .builder()
                    .loginId("wrongId")
                    .password("password123")
                    .build();

            given(authService.login(any(AuthDto.LoginRequest.class))).willThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").exists());
        }

        @Test
        @DisplayName("로그인 성공한 케이스")
        public void should_success_login() throws Exception {
            AuthDto.LoginRequest request = AuthDto.LoginRequest
                    .builder()
                    .loginId("wrongId")
                    .password("password123")
                    .build();

            AuthDto.TokenPair token = AuthDto.TokenPair.builder()
                    .accessToken("access_token_test")
                    .build();

            given(authService.login(any(AuthDto.LoginRequest.class))).willReturn(token);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("access_token_test"));
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class Logout {
        @Test
        @DisplayName("인증 실패")
        public void should_fail_logout_unauthorized() throws Exception {
            willThrow(new BusinessException(ErrorCode.UNAUTHORIZED))
                    .given(authService)
                    .logout(isNull(), anyString());
            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(new Cookie("refreshToken", "refresh_token_test"))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));

        }

        @Test
        @DisplayName("로그아웃 성공한 케이스")
        public void should_success_logout() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer access_token_test")
                            .cookie(new Cookie("refreshToken", "refresh_token_test"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class Refresh {
        @Test
        @DisplayName("파라미터 누락")
        public void should_fail_refresh_params_missing() throws Exception {
            willThrow(new BusinessException(ErrorCode.UNAUTHORIZED))
                    .given(authService)
                    .refresh(null);

            mockMvc.perform(post("/api/v1/auth/refresh"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("토큰 재발급 성공 케이스")
        public void should_success_refresh() throws Exception {
            // 새로운 토큰 발급
            AuthDto.TokenPair tokenSet = AuthDto.TokenPair
                    .builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .build();

            given(authService.refresh(anyString())).willReturn(tokenSet);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", "old-rt")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
        }
    }
}
