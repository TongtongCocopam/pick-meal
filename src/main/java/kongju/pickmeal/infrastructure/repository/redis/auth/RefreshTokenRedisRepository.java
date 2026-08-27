package kongju.pickmeal.infrastructure.repository.redis.auth;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import kongju.pickmeal.core.auth.RefreshToken;
import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, String> {
    void deleteByUserId(@NonNull Long userId);

    Optional<RefreshToken> findByUserId(Long userId);
}
