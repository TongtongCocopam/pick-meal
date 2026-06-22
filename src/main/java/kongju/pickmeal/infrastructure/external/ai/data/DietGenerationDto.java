package kongju.pickmeal.infrastructure.external.ai.data;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

public class DietGenerationDto {
    @Builder
    public record GenerateRequest(
            @NotNull
            LocalDate startDate,
            @NotNull
            Integer dailyMealCount
    ){
    }

    @Builder
    public record GenerateResponse(

    ){}
}
