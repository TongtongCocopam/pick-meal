package kongju.pickmeal.core.auth;

public interface RefreshTokenRepository {
    void deleteById(String loginId);
    RefreshToken save(RefreshToken refreshToken);
}
