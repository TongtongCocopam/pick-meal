package kongju.pickmeal.application.diet.event;

import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

import lombok.Builder;

import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;

@Builder
public record DietGenerationRequestedEventDto(
        Long userId,
        UUID generationId,
        DietGenerationDto.GenerateRequest request,
        LocalDate startDate,
        LocalDate endDate,
        List<Long> userMenuPickIds
) {
}
