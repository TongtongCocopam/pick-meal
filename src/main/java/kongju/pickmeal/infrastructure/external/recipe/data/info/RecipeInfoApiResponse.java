package kongju.pickmeal.infrastructure.external.recipe.data.info;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecipeInfoApiResponse(
        @JsonProperty("Grid_20150827000000000226_1")
        RecipeInfoGrid grid
) {
}
