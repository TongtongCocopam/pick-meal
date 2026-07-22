package kongju.pickmeal.infrastructure.repository.redis.auth;

import org.jspecify.annotations.NonNull;

import kongju.pickmeal.core.auth.RefreshToken;
import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, String> {
    void deleteById(@NonNull String id);
}
