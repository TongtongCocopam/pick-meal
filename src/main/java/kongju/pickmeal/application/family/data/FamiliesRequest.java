package kongju.pickmeal.application.family.data;

import lombok.Builder;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;


public class FamiliesRequest {
    @Builder
    public record Create(
            @NotBlank
            @Pattern(regexp = "^[a-zA-Z가-힣\\\\s]*${2,15}",
                    message = "가족 이름은 한글, 영문만 가능합니다.")
            String familyName
    ) {
    }

    @Builder
    public record Apply(
        @NotBlank(message = "초대 코드는 필수입니다.")
        @Size(min = 8, max = 8, message = "초대 코드는 8자리여야 합니다.")
        String invitationCode
    ){
    }
}
