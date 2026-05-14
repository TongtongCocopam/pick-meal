package kongju.pickmeal.application.auth;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.auth.RefreshToken;
import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.auth.data.AuthDto;
import kongju.pickmeal.core.auth.RefreshTokenRepository;
import kongju.pickmeal.common.exception.BusinessException;
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
    public AuthDto.TokenPair login(AuthDto.LoginRequest request) {
        User user = authenticate(request);

        // 액세스 토큰 발급
        String accessToken = jwtService.createAccessToken(user);
        // 리프레시 토큰 발급
        String refreshToken = jwtService.createRefreshToken(user);

        // 리프레시 토큰 레디스에 저장
        saveRefreshToken(user.getLoginId(), refreshToken);

        return AuthDto.TokenPair.builder()
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
    private User authenticate(AuthDto.LoginRequest request) {
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
     * @param authorizationHeader 액세스 토큰 담고 있음
     * @param refreshToken 리프레시 토큰
     */
    public void logout(String authorizationHeader, String refreshToken) {
        String accessToken = extractAccessToken(authorizationHeader);

        String loginId = checkRefreshTokenExpired(refreshToken);

        // 레디스 리프레쉬 토큰 삭제
        refreshTokenRepository.deleteById(loginId);

        if (jwtService.isValidAccessToken(accessToken)) {
            // 액세스 토큰 블랙 리스트 추가
            long expiration = jwtService.getAccessTokenExpiration(accessToken);

            if (expiration > 0L) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + accessToken,
                        "logout",
                        expiration,
                        TimeUnit.MILLISECONDS
                );
            }
        }
    }

    /**
     * 액세스 토큰 추출 후 유효한지 확인
     * @param authorizationHeader 헤더
     * @return 액세스 토큰
     */
    private String extractAccessToken(String authorizationHeader) {
        String accessToken = jwtService.extractAccessToken(authorizationHeader)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return accessToken;
    }

    /**
     * 리프레시 토큰에서 loginId추출
     * @param refreshToken 리프레시 토큰
     * @return loginId
     */
    private String checkRefreshTokenExpired(String refreshToken) {
        return jwtService.extractSubjectFromRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    /**
     * 토큰 재발급
     * @param oldRefreshToken 리프레시 토큰
     * @return 액세스, 리프레시 토큰
     */
    public AuthDto.TokenPair refresh(String oldRefreshToken) {
        User user = verifyToken(oldRefreshToken);

        // 토큰 재발급
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        // 토큰 저장
        saveRefreshToken(user.getLoginId(), refreshToken);

        return AuthDto.TokenPair
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 리프레시 토큰이 유효한지 확인하고 loginId반환
     * @param oldRefreshToken 리프레시 토큰
     * @return loginId
     */
    private User verifyToken(String oldRefreshToken) {
        // 리프레시 토큰 확인
        if (oldRefreshToken == null || !jwtService.isValidRefreshToken(oldRefreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 유저와 토큰이 일치하는가
        String userId = jwtService.extractSubjectFromRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        String savedToken = redisTemplate.opsForValue().get("rt:" + userId);
        if (savedToken == null || !savedToken.equals(oldRefreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 유저반환
        return userRepository.findByLoginId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

}
