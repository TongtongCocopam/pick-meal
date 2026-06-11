package kongju.pickmeal.infrastructure.external.recipe.foodsafety.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FoodSafetyRecipeInfoResponse(
        @JsonProperty("COOKRCP01")
        FoodSafetyRecipeInfo info
) {
}
