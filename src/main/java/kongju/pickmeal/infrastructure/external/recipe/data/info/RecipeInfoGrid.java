package kongju.pickmeal.infrastructure.external.recipe.data.info;

import java.util.List;

public record RecipeInfoGrid(
        int totalCnt,
        List<RecipeInfoRow> row
) {
}
