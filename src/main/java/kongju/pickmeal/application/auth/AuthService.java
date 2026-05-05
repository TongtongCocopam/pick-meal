package kongju.pickmeal.application.auth;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.auth.RefreshToken;
import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.auth.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 로그인 기능
     * 액세스토큰과 리프레시 토큰 발급
     *
     * @param request 아이디, 비밀번호
     * @return 액세스 토큰
     */
    public AuthResponse.Token login(AuthRequest.Login request) {
        User user = authenticate(request);

        // 액세스 토큰 발급
        String accessToken = jwtService.createAccessToken(user);
        // 리프레시 토큰 발급
        String refreshToken = jwtService.createRefreshToken(user);

        // 리프레시 토큰 레디스에 저장
        saveRefreshToken(user.getLoginId(), refreshToken);

        return AuthResponse.Token.builder()
                .accessToken(String.valueOf(accessToken))
                .refreshToken(String.valueOf(refreshToken))
                .build();
    }

    /**
     * 존재하는 유저인지, 비밀번호가 일치하는지 확인
     *
     * @param request 비밀번호
     * @return User 객체
     */
    private User authenticate(AuthRequest.Login request) {
        // 존재하는 유저인지 확인
        User user = userAuthenticate(request.loginId());

        // 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return user;
    }

    /**
     * 존재하는 유저인지 확인
     *
     * @param loginId 아이디
     * @return User 객체
     */
    private User userAuthenticate(String loginId) {
        // 존재하는 유저인지 확인
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
    }

    /**
     * 리프레시 토큰 레디스에 저장
     *
     * @param loginId 사용자 아이디
     * @param token   리프레시 토큰
     */
    private void saveRefreshToken(String loginId, String token) {
        long expiration = Duration.ofDays(14).toSeconds();
        RefreshToken refreshToken = new RefreshToken(loginId, token, expiration);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 로그아웃 기능
     * 리프레시 토큰 삭제, 액세스 토큰 블랙리스트 추가
     *
     * @param request 아이디, 액세스 토큰
     */
    public void logout(AuthRequest.Logout request) {
        userAuthenticate(request.loginId());

        // 레디스 리프레쉬 토큰 삭제
        refreshTokenRepository.deleteById(request.loginId());

        // 액세스 토큰 블랙 리스트 추가
        long expiration = jwtService.getExpiration(request.accessToken());
        if (expiration > 0L) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + request.accessToken(),
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public AuthResponse.Token refresh(AuthRequest.RefreshToken request, String oldRefreshToken) {
        // 2개 토큰 검증
        if (request.accessToken() == null || request.accessToken().isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        // 액세스 토큰이 만료되지 않았다면 에러 처리
        if (!jwtService.isAccessTokenExpired(request.accessToken())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        // 리프레시 토큰 확인
        if (!jwtService.validateRefreshToken(oldRefreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 유저와 토큰이 일치하는가
        Optional<String> userId = jwtService.getSubFromExpiredToken(request.accessToken());
        String savedToken = redisTemplate.opsForValue().get("rt:" + userId);
        if (!Objects.requireNonNull(savedToken).equals(oldRefreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        // 없다면 에러 처리
        Optional<User> user = Optional.of(userRepository.findByLoginId(String.valueOf(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED)));

        // 토큰 재발급
        String accessToken = jwtService.createAccessToken(user.get());
        String refreshToken = jwtService.createRefreshToken(user.get());
        saveRefreshToken(user.get().getLoginId(), refreshToken);

        return AuthResponse.Token
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void verifyRefreshToken(AuthRequest.RefreshToken request, HttpServletResponse hResponse) {

    }

}
