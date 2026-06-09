package kongju.pickmeal.infrastructure.external.recipe.data.ingredient;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecipeIngredientApiResponse(
        @JsonProperty("Grid_20150827000000000227_1")
        RecipeIngredientGrid grid
) {

}
