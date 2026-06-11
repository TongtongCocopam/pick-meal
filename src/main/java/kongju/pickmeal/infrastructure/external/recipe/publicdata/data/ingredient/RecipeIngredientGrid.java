package kongju.pickmeal.infrastructure.external.recipe.publicdata.data.ingredient;

import java.util.List;

public record RecipeIngredientGrid(
        int totalCnt,
        List<RecipeIngredientRow> row
) {
}
