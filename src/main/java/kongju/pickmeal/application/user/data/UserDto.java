package kongju.pickmeal.application.user.data;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Builder;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;


@Getter
public class UserDto {
    @Builder
    public record SignupRequest(
            @NotBlank
            @Size(min = 6, max = 15)
            String loginId,
            @NotBlank
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\W]{8,16}$",
                    message = "비밀번호는 영문과 숫자를 포함하여 8자 이상 16자 이하이어야 합니다."
            )
            String password,
            @NotBlank
            String passwordCheck,
            @NotBlank
            @Email
            String email,
            @NotBlank
            String nickname,
            @NotNull
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate birthDate
            ){}

    @Builder
    public record SignupResponse(
            Long userId,
            String nickname
    ){}
}
