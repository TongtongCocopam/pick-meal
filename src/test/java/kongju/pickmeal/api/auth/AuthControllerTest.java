package kongju.pickmeal.api.auth;


import jakarta.servlet.http.Cookie;
import kongju.pickmeal.api.security.JwtTokenFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;


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
            AuthRequest.Login request = AuthRequest.Login
                    .builder()
                    .loginId("wrongId")
                    .password("password123")
                    .build();

            given(authService.login(any(AuthRequest.Login.class))).willThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

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
            AuthRequest.Login request = AuthRequest.Login
                    .builder()
                    .loginId("wrongId")
                    .password("password123")
                    .build();

            AuthResponse.Token token = AuthResponse.Token.builder()
                    .accessToken("access_token_test")
                    .build();

            given(authService.login(any(AuthRequest.Login.class))).willReturn(token);

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
        @DisplayName("필수 파라미터 누락")
        public void should_fail_logout_params_missing() throws Exception {
            AuthRequest.Token request = AuthRequest.Token.builder()
                    .accessToken("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value(false));

        }

        @Test
        @DisplayName("로그아웃 성공한 케이스")
        public void should_success_logout() throws Exception {
            AuthRequest.Token request = AuthRequest.Token
                    .builder()
                    .accessToken("access")
                    .build();

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class Refresh {
        @Test
        @DisplayName("파라미터 누락")
        public void should_fail_refresh_params_missing() throws Exception {
            AuthRequest.Token request = AuthRequest.Token
                    .builder()
                    .accessToken("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .cookie(new Cookie("refreshToken", "old-rt")))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("토큰 재발급 성공 케이스")
        public void should_success_refresh() throws Exception {
            AuthRequest.Token request = AuthRequest.Token
                    .builder()
                    .accessToken("accessToken")
                    .build();

            // 새로운 토큰 발급
            AuthResponse.Token tokenSet = AuthResponse.Token
                    .builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .build();

            given(authService.refresh(any(), anyString())).willReturn(tokenSet);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .cookie(new Cookie("refreshToken", "old-rt")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=new-refresh-token")))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
        }
    }
}
