package kongju.pickmeal.api.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import kongju.pickmeal.application.auth.data.response.AuthResponse;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.application.auth.data.request.AuthRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
