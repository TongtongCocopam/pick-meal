package kongju.pickmeal.application.auth.data.request;

import jakarta.validation.constraints.NotBlank;
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

}
