package kongju.pickmeal.application.auth;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.api.security.JwtTokenFilter;
import kongju.pickmeal.core.auth.RefreshTokenRepository;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;


@ExtendWith(SpringExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenFilter jwtTokenFilter;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("로그인 테스트")
    class Login {
        @Test
        @DisplayName("아이디와 일치하는 유저를 찾지 못했을 경우 에러 처리")
        public void should_fail_loginId_not_found() {
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .loginId("test1234")
                    .password("password1234")
                    .build();

            given(userRepository.findByLoginId(anyString())).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
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

            assertThatThrownBy(() -> authService.login(request))
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

            AuthResponse.Token response = authService.login(request);

            assertThat(response.accessToken()).isEqualTo("mock_access_token");
            verify(userRepository).findByLoginId("test1234");
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class Logout {
        private AuthRequest.Logout createLogoutRequest() {
            return AuthRequest.Logout.builder()
                    .loginId("test1234")
                    .accessToken("accessToken")
                    .build();
        }

        @Test
        @DisplayName("존재하지 않는 유저")
        public void should_fail_logout_user_not_found() {
            AuthRequest.Logout request = createLogoutRequest();
            given(userRepository.findByLoginId(anyString())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.logout(request);
            });

            assertEquals(ErrorCode.LOGIN_FAILED, exception.getErrorCode());
            verify(refreshTokenRepository, times(0)).deleteById(anyString());

        }

        @Test
        @DisplayName("만료된 토큰으로 로그아웃")
        public void should_fail_logout_expired_token() {
            AuthRequest.Logout request = createLogoutRequest();
            User user = User.builder()
                    .loginId("test1234")
                    .password("password1234")
                    .build();
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(userRepository.findByLoginId(anyString())).willReturn(Optional.ofNullable(user));
            given(jwtService.getExpiration(any())).willReturn(0L);

            // 어떤 에러도 터지지 않아야 함
            assertDoesNotThrow(() -> {
                authService.logout(request);
            });
            // 레디스 템플릿이 아닌 valueOperations를 확인해야 함
            verify(valueOperations, times(0)).set(anyString(), anyString(), anyLong(), any());

        }


        @Test
        @DisplayName("잘못된 형식 토큰이 필터에 걸릴 경우")
        public void should_fail_logout_filtered_token() throws Exception {
            String token = "mock_refresh_token";
            // 필터와 서블릿을 가짜로 생성
            MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
            MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
            MockFilterChain mockFilterChain = new MockFilterChain();

            // 가짜 인증 붙이기
            mockHttpServletRequest.addHeader("Authorization", "Bearer " + token);
            // 빈 객체 반환
            given(jwtService.getSubFromAccessToken(token)).willReturn(Optional.empty());

            jwtTokenFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
            // 토큰이 통과하지 못하면 null반환
            assertNull(SecurityContextHolder.getContext().getAuthentication());

        }


        @Test
        @DisplayName("리프레시 토큰이 헤더가 없는 경우")
        public void should_fail_logout_not_existed_token() throws Exception {

            MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
            MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
            MockFilterChain mockFilterChain = new MockFilterChain();
            jwtTokenFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("로그아웃 성공")
        public void should_success_logout() {
            AuthRequest.Logout request = createLogoutRequest();
            User user = User.builder()
                    .loginId("test1234")
                    .password("password1234")
                    .build();
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(userRepository.findByLoginId(anyString())).willReturn(Optional.ofNullable(user));
            given(jwtService.getExpiration("accessToken")).willReturn(3600000L);

            authService.logout(request);

            // 리프레시 토큰 삭제 확인
            verify(refreshTokenRepository, times(1)).deleteById(anyString());

            // 블랙리스트 만료 시간 확인
            verify(valueOperations, times(1)).set(
                    eq("blacklist:" + "accessToken"),
                    eq("logout"),
                    eq(3600000L),
                    eq(TimeUnit.MILLISECONDS)
            );

        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class Refresh {
        private AuthRequest.RefreshToken createRefreshTokenRequest() {
            return AuthRequest.RefreshToken
                    .builder()
                    .accessToken("accessToken")
                    .build();
        }
        private User createUser() {
            return User.builder()
                    .loginId("test1234")
                    .password("password1234")
                    .build();
        }

        @Test
        @DisplayName("토큰 재발급 성공")
        public void should_success_refresh() {
            User user = createUser();
            // 리프레시 토큰 시간, 액세스 토큰이 발급 시간 확인
            AuthRequest.RefreshToken request = createRefreshTokenRequest();
            String oldRefreshToken = "oldRefreshToken";

            // 액세스 토큰 만료 안됨
            given(jwtService.isAccessTokenExpired(anyString())).willReturn(true);
            // 리프레시 토큰 만료
            given(jwtService.validateRefreshToken(anyString())).willReturn(true);
            // 만료된 액세스 토큰에서 유저 아이디 반환
            given(jwtService.getSubFromExpiredToken(anyString())).willReturn(Optional.of("test1234"));
            // 아이디로 토큰꺼내기
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("rt:test1234")).willReturn("oldRefreshToken");
            // 유저 객체 반환
            given(userRepository.findByLoginId(anyString())).willReturn(Optional.ofNullable(user));

            given(jwtService.createAccessToken(user)).willReturn("newAccessToken");
            given(jwtService.createRefreshToken(user)).willReturn("newRefreshToken");

            AuthResponse.Token response = authService.refresh(request, oldRefreshToken);

            assertThat(response.accessToken()).isEqualTo("newAccessToken");
            assertThat(response.refreshToken()).isEqualTo("newRefreshToken");

            verify(refreshTokenRepository, times(1)).save(any());
            verify(userRepository, times(1)).findByLoginId("test1234");

        }

        @Test
        @DisplayName("액세스 토큰이 만료되지 않았을 경우 에러")
        public void should_fail_refresh_access_token_not_expired() {
            AuthRequest.RefreshToken request = createRefreshTokenRequest();
            String oldRefreshToken = "oldRefreshToken";
            given(jwtService.isAccessTokenExpired(anyString())).willReturn(false);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.refresh(request, oldRefreshToken);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);

        }

        @Test
        @DisplayName("리프레시 토큰이 만료되었을때 or 없을 때")
        public void should_fail_refresh_refresh_token_expired_or_not_found() {
            AuthRequest.RefreshToken request = createRefreshTokenRequest();
            String oldRefreshToken = "oldRefreshToken";
            given(jwtService.isAccessTokenExpired(anyString())).willReturn(true);
            given(jwtService.validateRefreshToken(anyString())).willReturn(false);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.refresh(request, oldRefreshToken);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);

        }

        @Test
        @DisplayName("유저와 토큰 정보가 맞지 않을때")
        public void should_fail_refresh_user_token_not_match() {
            AuthRequest.RefreshToken request = createRefreshTokenRequest();
            String oldRefreshToken = "kk";

            // 액세스 토큰 만료 안됨
            given(jwtService.isAccessTokenExpired(anyString())).willReturn(true);
            // 리프레시 토큰 만료
            given(jwtService.validateRefreshToken(anyString())).willReturn(true);
            // 만료된 액세스 토큰에서 유저 아이디 반환
            given(jwtService.getSubFromExpiredToken(anyString())).willReturn(Optional.of("test1234"));
            // 아이디로 토큰꺼내기
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("rt:test1234")).willReturn("oldRefreshToken");
            // 유저 객체 반환
            given(userRepository.findByLoginId(anyString())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.refresh(request, oldRefreshToken);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        }

    }
}
