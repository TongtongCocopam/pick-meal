package kongju.pickmeal.infrastructure.external.recipe.foodsafety.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FoodSafetyRecipeRow(
        @JsonProperty("RCP_PARTS_DTLS")
        String recipeParts,

        @JsonProperty("RCP_NM")
        String menuName,

        @JsonProperty("RCP_PAT2")
        String dishType,

        @JsonProperty("INFO_WGT")
        String weight,

        @JsonProperty("INFO_ENG")
        String kcal,

        @JsonProperty("INFO_CAR")
        String carbs,

        @JsonProperty("INFO_PRO")
        String protein,

        @JsonProperty("INFO_FAT")
        String fat,

        @JsonProperty("INFO_NA")
        String sodium,

        @JsonProperty("HASH_TAG")
        String mainIngredient
) {
}
