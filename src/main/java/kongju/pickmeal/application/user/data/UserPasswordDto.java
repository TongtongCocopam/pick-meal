package kongju.pickmeal.application.user.data;

import lombok.Builder;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;


public class UserPasswordDto {
    @Builder
    public record UpdateRequest(
            @NotBlank(message = "현재 비밀번호를 입력해주세요.")
            String currentPassword,
            @NotBlank
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\W]{8,16}$",
                    message = "비밀번호는 영문과 숫자를 포함하여 8자 이상 16자 이하이어야 합니다."
            )
            String newPassword,
            @NotBlank(message = "비밀번호 확인은 필수입니다.")
            String confirmPassword
    ){}
}
