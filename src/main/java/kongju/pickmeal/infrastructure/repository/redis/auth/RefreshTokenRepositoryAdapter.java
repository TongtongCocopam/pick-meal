package kongju.pickmeal.infrastructure.repository.redis.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.auth.RefreshToken;
import kongju.pickmeal.core.auth.RefreshTokenRepository;


@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Override
    public void deleteById(String loginId) {
        refreshTokenRedisRepository.deleteById(loginId);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRedisRepository.save(refreshToken);
    }
}
