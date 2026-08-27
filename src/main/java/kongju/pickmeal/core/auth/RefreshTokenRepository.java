package kongju.pickmeal.core.auth;

import java.util.Optional;

public interface RefreshTokenRepository {
    void deleteByUserId(Long userId);

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByUserId(Long userId);
}
