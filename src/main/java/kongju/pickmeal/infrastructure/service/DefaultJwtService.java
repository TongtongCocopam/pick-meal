package kongju.pickmeal.infrastructure.service;

import java.util.Date;
import java.util.Optional;
import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import javax.crypto.SecretKey;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
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
            @Value("${jwt.refresh-secret}") String refresh_secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        // 비밀 키 생성
        this.accessKey = Keys.hmacShaKeyFor(access_secret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refresh_secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * 토큰 생성
     *
     * @param user           사용자
     * @param expirationTime 만료 시간
     * @param signingKey     비밀키
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

    @Override
    public String createAccessToken(User user) {
        return createToken(user, accessExpiration, accessKey);
    }

    @Override
    public String createRefreshToken(User user) {
        return createToken(user, refreshExpiration, refreshKey);
    }


    /**
     * 액세스 토큰 추출
     *
     * @param authorizationHeader 헤더
     * @return 액세스 토큰
     */
    @Override
    public Optional<String> extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String accessToken = authorizationHeader.substring(7).trim();

        if (accessToken.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(accessToken);
    }

    /**
     * 액세스 토큰의 남은 만료 시간 계산
     *
     * @param accessToken 액세스 토큰
     * @return 남은 만료 시간
     */
    @Override
    public Long getAccessTokenExpiration(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            Date expirationDate = claims.getExpiration();

            long now = new Date().getTime();
            long diff = expirationDate.getTime() - now;

            return diff > 0 ? diff : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 액세스 토큰 유효성 검증
     *
     * @param accessToken 액세스 토큰
     * @return 유효한지 boolean
     */
    @Override
    public boolean isValidAccessToken(String accessToken) {
        return isValid(accessToken, accessKey);
    }

    /**
     * 리프레시 토큰 유효성 검증
     *
     * @param refreshToken 리프레시 토큰
     * @return 유효한지 boolean
     */
    @Override
    public boolean isValidRefreshToken(String refreshToken) {
        return isValid(refreshToken, refreshKey);
    }

    /**
     * accessToken 만료 여부 확인
     *
     * @param accessToken 액세스 토큰
     * @return 만료 여부
     */
    @Override
    public boolean isExpiredAccessToken(String accessToken) {
        try {
            // 정상적으로 파싱되면 만료되지 않음
            getClaimsFromToken(accessToken, accessKey);
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 유효기간이 지남
            return true;
        } catch (Exception e) {
            // 알 수 없는 에러 전부
            return false;
        }
    }

    /**
     * accessToken에서 userId 추출
     *
     * @param accessToken 액세스 토큰
     * @return 로그인 아이디
     */
    @Override
    public Optional<Long> extractSubjectFromAccessToken(String accessToken) {
        return extractSubject(accessToken, accessKey);
    }

    /**
     * refreshToken에서 userId 추출
     *
     * @param refreshToken 리프레시 토큰
     * @return 로그인 아이디
     */
    @Override
    public Optional<Long> extractSubjectFromRefreshToken(String refreshToken) {
        return extractSubject(refreshToken, refreshKey);
    }

    /**
     * 만료된 accessToken에서 userId 추출
     *
     * @param accessToken 액세스 토큰
     * @return 로그인 아이디
     */
    @Override
    public Optional<Long> extractSubjectFromExpiredAccessToken(String accessToken) {
        try {
            // 정상 토큰인지 확인
            Claims claims = getClaimsFromToken(accessToken, accessKey);
            return parseUserId(claims.getSubject());
        } catch (ExpiredJwtException e) {
            // 만료 에러 객체는 파싱된 데이터가 보관되어 있으므로 id추출
            return parseUserId(e.getClaims().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            // 위조된 토큰일 경우
            return Optional.empty();
        }
    }

    /**
     * subject에서 userId꺼내기
     *
     * @param subject 유저 아이디를 담은 데이터
     * @return userId
     */
    private Optional<Long> parseUserId(String subject) {
        if (subject == null || subject.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(subject));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 토큰 검증 및 claims반환
     *
     * @param token      토큰
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
     * 리프레시 토큰 확인 및 userId반환
     *
     * @param token 리프레시 토큰
     * @return userId
     */
    public Optional<Long> extractSubject(String token, SecretKey signingKey) {
        try {
            // jjwt를 사용하여 토큰 내부의 claims을 가져옴
            Claims claims = getClaimsFromToken(token, signingKey);

            return parseUserId(claims.getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 토큰과 키를 이용해 유요한 토큰인지 확인
     *
     * @param token      토큰
     * @param signingKey 키
     * @return boolean
     */
    public boolean isValid(String token, SecretKey signingKey) {
        try {
            getClaimsFromToken(token, signingKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
