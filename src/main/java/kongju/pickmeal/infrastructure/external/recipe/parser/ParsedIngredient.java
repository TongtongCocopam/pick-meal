package kongju.pickmeal.infrastructure.external.recipe.parser;

import lombok.Builder;

@Builder
public record ParsedIngredient(
        String ingredientName,
        String quantityText
) {
}
