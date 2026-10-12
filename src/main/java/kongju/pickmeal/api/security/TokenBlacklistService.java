package kongju.pickmeal.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlacklisted(String token) {
        return redisTemplate.opsForValue().get("blacklist:" + token) != null;
    }
}