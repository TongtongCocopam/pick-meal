package kongju.pickmeal.application.auth.data.response;

import lombok.Getter;

@Getter
public class AuthResponse {
    public record Token(
            String accessToken,
            String refreshToken
    ) {
    }
}
