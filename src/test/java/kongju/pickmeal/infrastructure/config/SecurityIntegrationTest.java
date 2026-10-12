package kongju.pickmeal.infrastructure.config;

import java.util.Optional;

import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.type.UserRole;
import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.repository.UserRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoSpyBean
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        doReturn(valueOperations).when(redisTemplate).opsForValue();

        given(valueOperations.get(anyString())).willReturn(null);
    }

    @Test
    void should_return_unauthorized_when_access_token_is_invalid() throws Exception {
        mockMvc.perform(get("/api/v1/diets")
                        .param("month", "2026-06")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer invalid-token"
                        ))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_authenticate_when_access_token_is_valid() throws Exception {
        User user = mock(User.class);

        given(user.getId()).willReturn(1L);

        given(user.getRole()).willReturn(UserRole.LEADER);


        // 실제 JwtService로 실제 Access Token 생성
        String accessToken = jwtService.createAccessToken(user);


        // JwtTokenFilter가 JWT에서 userId = 1을 꺼낸 뒤
        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));


        // 실제 JWT를 Authorization Header에 담아서 요청
        mockMvc.perform(get("/api/v1/diets")
                        .param("month", "2026-06")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        ))
                .andDo(print())
                .andExpect(status().isOk());
    }
}