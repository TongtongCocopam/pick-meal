package kongju.pickmeal.application.auth;

import java.util.Optional;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;
import kongju.pickmeal.core.user.User;

@ExtendWith(SpringExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("아이디와 일치하는 유저를 찾지 못했을 경우 에러 처리")
    public void should_fail_loginId_not_found() {
        AuthRequest.Login request = AuthRequest.Login.builder()
                .loginId("test1234")
                .password("password1234")
                .build();

        given(userRepository.findByLoginId(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request, mockHttpServletResponse))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);
        verify(userRepository).findByLoginId("test1234");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않는 경우 에러 처리")
    public void should_fail_password_not_correct() {
        AuthRequest.Login request = AuthRequest.Login.builder()
                .loginId("test1234")
                .password("password1234")
                .build();
        User user = User.builder()
                .loginId("test1234")
                .password("password1234")
                .build();

        given(userRepository.findByLoginId("test1234")).willReturn(Optional.of(user));

        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request, mockHttpServletResponse))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

    }

    @Test
    @DisplayName("로그인 성공한 케이스")
    public void should_success_login() {
        AuthRequest.Login request = AuthRequest.Login.builder()
                .loginId("test1234")
                .password("password1234")
                .build();
        User user = User.builder()
                .loginId("test1234")
                .password("password1234")
                .build();

        given(userRepository.findByLoginId("test1234")).willReturn(Optional.of(user));

        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        // JwtService 토큰 생성 Mocking
        given(jwtService.createAccessToken(any())).willReturn("mock_access_token");
        given(jwtService.createRefreshToken(any())).willReturn("mock_refresh_token");

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        AuthResponse.Token response = authService.login(request, mockHttpServletResponse);

        assertThat(response.accessToken()).isEqualTo("mock_access_token");
        verify(userRepository).findByLoginId("test1234");
    }
}
