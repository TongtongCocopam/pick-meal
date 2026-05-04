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
    Optional<String> getSubFromAccessToken(String token) ;
    Optional<String> getSubFromRefreshToken(String token) ;

}
