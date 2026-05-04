package kongju.pickmeal.api.auth;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;
import org.springframework.web.client.RestTemplate;


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
            AuthRequest.Login request = new AuthRequest.Login("wrongId", "password123");
            given(authService.login(any(AuthRequest.Login.class), any(HttpServletResponse.class))).willThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

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
            AuthRequest.Login request = new AuthRequest.Login("wrongId", "password123");

            AuthResponse.Token token = AuthResponse.Token.builder()
                    .accessToken("access_token_test")
                    .build();

            given(authService.login(any(AuthRequest.Login.class), any(HttpServletResponse.class))).willReturn(token);

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
            AuthRequest.Logout request = AuthRequest.Logout.builder()
                    .loginId("")
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
            AuthRequest.Logout request = new AuthRequest.Logout("user1", "access");

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }


}
