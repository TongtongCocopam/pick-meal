package kongju.pickmeal.infrastructure.external.ai.data;

import java.util.UUID;
import java.time.YearMonth;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;

import kongju.pickmeal.core.diet.type.DietGenerationStatus;


public class DietGenerationDto {
    @Builder
    public record GenerateRequest(
            @NotNull
            @JsonFormat(pattern = "yyyy-MM")
            YearMonth targetMonth,
            @NotNull
            Integer dailyMealCount
    ) {
    }

    @Builder
    public record GenerateResponse(
            UUID generationId,
            DietGenerationStatus status
    ) {
    }
}
