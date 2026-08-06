package kongju.pickmeal.infrastructure.repository.redis.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.auth.RefreshToken;
import kongju.pickmeal.core.auth.RefreshTokenRepository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenRedisRepository.deleteByUserId(userId);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRedisRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return refreshTokenRedisRepository.findByUserId(userId);
    }
}
