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
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        // 비밀 키 생성
        this.accessKey = Keys.hmacShaKeyFor(access_secret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(represh_secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * 토큰 생성
     * @param user 사용자
     * @param expirationTime 만료 시간
     * @param signingKey 비밀키
     * @return 토큰 반환
     */
    private String createToken(User user, long expirationTime, SecretKey signingKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                // 토큰 주인 저장
                .subject(String.valueOf(user.getId()))
                // 발행 시간
                .issuedAt(now)
                // 만료 시간
                .expiration(expiryDate)
                // 해당 키로 서명
                .signWith(signingKey)
                .compact();
    }

    /**
     * 토큰 남은 유효시간 계산
     * @param token 액세스 토큰
     * @return 만료 시간 반환
     */
    @Override
    public Long getExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expirationDate = claims.getExpiration();

            long now = new Date().getTime();
            long diff = expirationDate.getTime() - now;

            return diff > 0 ? diff : 0L;
        } catch (Exception e) {
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

    /**
     * 토큰 검증 및 claims반환
     * @param token 토큰
     * @param signingKey 키
     * @return claims객체
     */
    private Claims getClaimsFromToken(String token, SecretKey signingKey) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 액세스 토큰 확인 및 loginId반환
     * @param token 액세스 토큰
     * @return loginId
     */
    @Override
    public Optional<String> extractSubject(String token) {
        try {
            // jjwt를 사용하여 토큰 내부의 claims을 가져옴
            Claims claims = getClaimsFromToken(token, accessKey);

            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 액세스 토큰이 만료되었는지 확인
     * @param token 액세스 토큰
     * @return 만료되었는지 boolean반환
     */
    @Override
    public boolean isExpired(String token) {
        try {
            // 정상적으로 파싱되면 만료되지 않음
            getClaimsFromToken(token,accessKey);
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 유효기간이 지남
            return true;
        } catch (Exception e) {
            // 알 수 없는 에러 전부
            return false;
        }
    }

    @Override
    public Optional<String> extractSubjectFromExpired(String token){
        try{
            // 정상 토큰인지 확인
            return extractSubject(token);
        }catch (Exception e){
            // 만료 예외 발생 확인
            if(e instanceof io.jsonwebtoken.ExpiredJwtException){
                // 만료 에러 객체는 파싱된 데이터가 보관되어 있으므로 id추출
                return Optional.ofNullable(((io.jsonwebtoken.ExpiredJwtException) e).getClaims().getSubject());
            }
            // 위조된 토큰일 경우
            return Optional.empty();
        }
    }

    @Override
    public boolean isValid(String token){
        try{
            getClaimsFromToken(token,refreshKey);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
