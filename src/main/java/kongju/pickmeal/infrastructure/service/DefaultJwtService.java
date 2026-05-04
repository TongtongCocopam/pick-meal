package kongju.pickmeal.infrastructure.service;

import java.util.Date;
import java.util.Optional;

import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.service.JwtService;


@Component
public class DefaultJwtService implements JwtService {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    @Autowired
    public DefaultJwtService(
        @Value("${jwt.access-secret}") String access_secret,
        @Value("${jwt.refresh-secret}") String represh_secret,
        @Value("${jwt.access-expiration}") long accessExpiration,
        @Value("${jwt.refresh-expiration}")  long refreshExpiration) {
        this.accessKey = Keys.hmacShaKeyFor(access_secret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(represh_secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    private String createToken(User user, long expirationTime, SecretKey signingKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Long getExpiration(String token){
        try{
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expirationDate = claims.getExpiration();

            long now = new Date().getTime();
            long diff = expirationDate.getTime() - now;

            return diff > 0 ? diff : 0L;
        }catch(Exception e){
            return 0L;
        }
    }

    @Override
    public String createAccessToken(User user) {
        return createToken(user, accessExpiration, accessKey);
    }

    @Override
    public String createRefreshToken(User user) {
        return createToken(user, refreshExpiration, refreshKey);
    }

    @Override
    public Optional<String> getSubFromAccessToken(String token) {
        return getSubFromToken(token, accessKey);
    }

    @Override
    public Optional<String> getSubFromRefreshToken(String token) {
        return getSubFromToken(token, refreshKey);
    }

    public Optional<String> getSubFromToken(String token, SecretKey signingKey) {
        try {
            // jjwt를 사용하여 토큰 내부의 claims을 가져옴
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
