package kongju.pickmeal.application.user.data;

import java.math.BigDecimal;

import lombok.Builder;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import kongju.pickmeal.core.user.type.Gender;


public class UserHealthDto {
    @Builder
    public record UpdateRequest(
            @NotNull
            Gender gender,

            @NotNull(message = "키는 필수입니다.")
            @DecimalMin(value = "50.0", message = "키는 50cm 이상이어야 합니다.")
            @DecimalMax(value = "250.0", message = "키는 250cm 이하이어야 합니다.")
            @Digits(integer = 3, fraction = 1, message = "키는 소수점 1자리까지 입력 가능합니다.")
            BigDecimal height,

            @NotNull(message = "몸무게는 필수입니다.")
            @DecimalMin(value = "10.0", message = "몸무게는 10kg 이상이어야 합니다.")
            @DecimalMax(value = "450.0", message = "몸무게는 450kg 이하이어야 합니다.")
            @Digits(integer = 3, fraction = 1, message = "몸무게는 소수점 1자리까지 입력가능합니다.")
            BigDecimal weight
    ){}
}
