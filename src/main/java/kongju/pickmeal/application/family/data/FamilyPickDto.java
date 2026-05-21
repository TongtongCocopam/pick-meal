package kongju.pickmeal.application.family.data;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public class FamilyPickDto {

    @Builder
    public record UpdateConfigRequest(
            @NotNull
            Boolean isAutoAllocations,
            @PositiveOrZero
            Long defaultAllocations,
            List<pickAllocations> pickAllocations
    ) {
        @Builder
        public record pickAllocations(
                Long userId,
                @PositiveOrZero
                Long pickCount
        ) {
        }

    }

    @Builder
    public record ConfigResponse(
            Boolean isAutoAllocations
    ) {
    }
}
