package kongju.pickmeal.core.auth;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "refreshToken")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    private Long userId;

    private String token;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private long expiration;

    @Builder
    public RefreshToken(Long userId, String token, Long expiration) {
        this.userId = userId;
        this.token = token;
        this.expiration = expiration;
    }

}
