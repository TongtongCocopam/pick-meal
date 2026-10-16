package kongju.pickmeal.application.family.data;

import lombok.Builder;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;


public class FamilyDto {
    @Builder
    public record CreateRequest(
            @NotBlank
            @Pattern(regexp = "^[가-힣a-zA-Z0-9]+${2,15}",
                    message = "가족 이름은 한글, 영문, 숫자만 가능합니다.")
            String familyName
    ){}
    @Builder
    public record CreateResponse(
            String familyName,
            String invitationCode
    ){}

}
