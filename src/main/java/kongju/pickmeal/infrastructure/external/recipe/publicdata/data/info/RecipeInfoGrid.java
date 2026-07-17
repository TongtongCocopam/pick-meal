package kongju.pickmeal.infrastructure.external.recipe.publicdata.data.info;

import java.util.List;

public record RecipeInfoGrid(
        int totalCnt,
        List<RecipeInfoRow> row
) {
}
