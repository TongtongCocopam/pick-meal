package kongju.pickmeal.application.user.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberRequest {
    public record Register(
            @NotBlank
            @Size(min = 6, max = 15)
            String loginId,
            @NotBlank
            @Size(min = 8, max = 16)
            String password,
            @NotBlank
            String passwordCheck,
            @NotBlank
            @Email
            String email,
            @NotBlank
            String nickName,
            @NotBlank
            String birthDate
    ){}

}
