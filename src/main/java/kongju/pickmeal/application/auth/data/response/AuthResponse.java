package kongju.pickmeal.application.auth.data.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthResponse {
    @Builder
    public record Token(
            String accessToken
    ) {
    }
}
