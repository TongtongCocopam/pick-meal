package kongju.pickmeal.infrastructure.external.ai.data;

import java.util.UUID;
import java.time.LocalDate;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;

import kongju.pickmeal.core.diet.type.DietGenerationStatus;


public class DietGenerationDto {
    @Builder
    public record GenerateRequest(
            @NotNull
            LocalDate startDate,
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
