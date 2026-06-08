package kongju.pickmeal.infrastructure.external.recipe.data.ingredient;

import java.util.List;

public record RecipeIngredientGrid(
        int totalCnt,
        List<RecipeIngredientRow> row
) {
}
