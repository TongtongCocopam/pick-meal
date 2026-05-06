package kongju.pickmeal.application.family.data;

import lombok.Builder;
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
}
