package kongju.pickmeal.application.auth.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthRequest {
    @Builder
    public record Login(
            @NotBlank
            String loginId,
            @NotBlank
            String password
    ){}
    @Builder
    public record Logout(
            @NotBlank
            String loginId,
            @NotBlank
            String accessToken
    ){}
}
