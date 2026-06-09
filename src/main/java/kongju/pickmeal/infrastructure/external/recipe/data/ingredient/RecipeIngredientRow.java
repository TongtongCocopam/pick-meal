package kongju.pickmeal.infrastructure.external.recipe.data.ingredient;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecipeIngredientRow(
        @JsonProperty("ROW_NUM")
        Long rowNum,

        @JsonProperty("RECIPE_ID")
        Long recipeId,

        @JsonProperty("IRDNT_SN")
        Integer ingredientSequence,

        @JsonProperty("IRDNT_NM")
        String ingredientName,

        @JsonProperty("IRDNT_CPCTY")
        String quantityText,

        @JsonProperty("IRDNT_TY_CODE")
        String ingredientTypeCode,

        @JsonProperty("IRDNT_TY_NM")
        String ingredientTypeName

        ) {
}
