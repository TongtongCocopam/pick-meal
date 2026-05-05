package kongju.pickmeal.core.service;

import kongju.pickmeal.core.user.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Optional;

@Service
public interface JwtService {
    String createAccessToken(User user);

    String createRefreshToken(User user);

    // 남은 만료시간 계산
    Long getExpiration(String token);

    Optional<String> getSubFromAccessToken(String token);

    Optional<String> getSubFromRefreshToken(String token);
    // 만료된 토큰 확인
    boolean isAccessTokenExpired(String token);
    // 만료된 토큰에서 loginId반환
    Optional<String> getSubFromExpiredToken(String token);
    // 리프레시 토큰 만료 확인
    boolean validateRefreshToken(String token);
}
