package kongju.pickmeal.application.auth;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 로그인 기능
     * 액세스토큰과 리프레시 토큰 발급
     * @param request 아이디, 비밀번호
     * @param response 쿠키 정보를 담을 파라미터
     * @return 액세스 토큰
     */
    public AuthResponse.Token login(AuthRequest.Login request, HttpServletResponse response) {
        User user = authenticate(request);

        // 액세스 토큰 발급
        String accessToken = jwtService.createAccessToken(user);
        // 리프레시 토큰 발급
        String refreshToken = jwtService.createRefreshToken(user);

        // 리프레시 토큰 레디스에 저장
        saveRefreshToken(user.getId(), String.valueOf(refreshToken));
        // 쿠키에 담기
        ResponseCookie cookie = ResponseCookie.from("refreshToken", String.valueOf(refreshToken))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14).toSeconds())
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return AuthResponse.Token.builder()
                .accessToken(String.valueOf(accessToken))
                .build();
    }

    /**
     * 존재하는 유저인지, 비밀번호가 일치하는지 확인
     * @param request 아이디, 비밀번호
     * @return User 객체
     */
    private User authenticate(AuthRequest.Login request) {
        // 존재하는 유저인지 확인
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        // 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return user;
    }

    /**
     * 리프레시 토큰 레디스에 저장
     * @param userId 사용자 아이디
     * @param refreshToken 리프레시 토큰
     */
    private void saveRefreshToken(Long userId, String refreshToken) {
        String key = "rt:" + userId;

        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofDays(14));
    }

}
