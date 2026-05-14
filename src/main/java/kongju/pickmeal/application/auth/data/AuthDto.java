package kongju.pickmeal.application.auth.data;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;


public class AuthDto {
    @Builder
    public record LoginRequest(
            @NotBlank
            String loginId,
            @NotBlank
            String password
    ){}
    @Builder
    public record TokenPair(
            String accessToken,
            String refreshToken
    ){}
    @Builder
    public record AccessTokenResponse(
            String accessToken
    ){}

}
