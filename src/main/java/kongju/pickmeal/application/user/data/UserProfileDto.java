package kongju.pickmeal.application.user.data;

import java.time.LocalDate;

import lombok.Builder;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;


public class UserProfileDto {
    @Builder
    public record UpdateRequest(
            @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하만 가능합니다.")
            String nickname,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            @Past(message = "생년월일은 과거 날짜여야 합니다.")
            LocalDate birthDate
    ){}

    @Builder
    public record UpdateResponse(
            Long id,
            String nickname,
            LocalDate birthDate,
            String email,
            String loginId
    ){
    }
}
