package kongju.pickmeal.core.service;

import kongju.pickmeal.core.user.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Optional;

@Service
public interface JwtService {
    // 액세스 토큰 생성
    String createAccessToken(User user);
    // 리프레시 토큰 생성
    String createRefreshToken(User user);

    // Authorization Header에서 Bearer accessToken 추출
    Optional<String> extractAccessToken(String authorizationHeader);

    // accessToken 남은 만료 시간 계산
    Long getAccessTokenExpiration(String accessToken);

    // accessToken 유효성 검증
    boolean isValidAccessToken(String accessToken);

    // refreshToken 유효성 검증
    boolean isValidRefreshToken(String refreshToken);

    // accessToken 만료 여부 확인
    boolean isExpiredAccessToken(String accessToken);

    // accessToken에서 userId 추출
    Optional<Long> extractSubjectFromAccessToken(String accessToken);

    // refreshToken에서 userId 추출
    Optional<Long> extractSubjectFromRefreshToken(String refreshToken);

    // 만료된 accessToken에서 userId 추출
    Optional<Long> extractSubjectFromExpiredAccessToken(String accessToken);
}
