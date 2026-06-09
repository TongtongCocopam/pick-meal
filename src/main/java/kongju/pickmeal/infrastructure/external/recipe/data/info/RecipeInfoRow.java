package kongju.pickmeal.infrastructure.external.recipe.data.info;

import com.fasterxml.jackson.annotation.JsonProperty;


public record RecipeInfoRow(
        @JsonProperty("RECIPE_ID")
        Long recipeId,

        @JsonProperty("RECIPE_NM_KO")
        String recipeName,

        @JsonProperty("NATION_NM")
        String nationName,

        @JsonProperty("TY_NM")
        String typeName,

        @JsonProperty("CALORIE")
        String kcal
) {
}
