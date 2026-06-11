package kongju.pickmeal.infrastructure.external.recipe.foodsafety.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FoodSafetyRecipeInfo(
        @JsonProperty("total_count")
        Long totalCount,
        List<FoodSafetyRecipeRow> row
) {
}
