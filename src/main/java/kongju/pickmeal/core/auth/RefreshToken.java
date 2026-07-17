package kongju.pickmeal.core.auth;


import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "refreshToken")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    private String loginId;

    private String token;

    @TimeToLive
    private long expriration;

    @Builder
    public RefreshToken(String loginId, String token, Long expriration) {
        this.loginId = loginId;
        this.token = token;
        this.expriration = expriration;
    }

}
