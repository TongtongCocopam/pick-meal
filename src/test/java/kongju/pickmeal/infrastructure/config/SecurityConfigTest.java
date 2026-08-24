package kongju.pickmeal.infrastructure.config;

import kongju.pickmeal.application.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kongju.pickmeal.api.user.UserController;
import kongju.pickmeal.core.service.JwtService;
import org.springframework.data.redis.core.RedisTemplate;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.api.security.CustomAccessDeniedHandler;
import kongju.pickmeal.api.security.CustomAuthenticationEntryPoint;


@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
public class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    UserService userService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("공개 엔드포인트는 인증 없이 접근 가능")
    void should_permit_public_endpoint_without_authentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus())
                                .isNotEqualTo(HttpStatus.UNAUTHORIZED.value())
                );
    }

    @Test
    @DisplayName("보호된 엔드포인트는 인증 없이 접근 불가")
    void should_reject_protected_endpoint_without_authentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("인증된 사용자는 보호된 엔드포인트 접근 가능")
    void should_allow_protected_endpoint_when_authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus())
                                .isNotEqualTo(HttpStatus.UNAUTHORIZED.value())
                );
    }
}
