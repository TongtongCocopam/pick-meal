package kongju.pickmeal.infrastructure.external.recipe.mapper;

import org.springframework.stereotype.Component;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import kongju.pickmeal.core.diet.type.IngredientUnit;
import kongju.pickmeal.infrastructure.external.recipe.data.ingredient.RecipeIngredientRow;

@Component
public class IngredientMapper {

    public String normalizeIngredientName(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return null;
        }
        return ingredientName.trim();
    }

    public MenuIngredient toMenuIngredient(
            RecipeIngredientRow row,
            Menu menu,
            Ingredient ingredient

    ) {
        return MenuIngredient.create(
                menu,
                ingredient,
                row.quantityText(),
                parseQuantity(row.quantityText()),
                parseUnit(row.quantityText())
        );
    }

    private Double parseQuantity(String quantityText) {
        if (quantityText == null || quantityText.isBlank()) {
            return null;
        }

        String text = quantityText.trim();

        // "20g", "200ml" 같은 단순 케이스만 우선 처리
        if (text.contains("/")) {
            return null;
        }

        String number = text.replaceAll("[^0-9.]", "");

        if (number.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private IngredientUnit parseUnit(String quantityText) {
        if (quantityText == null || quantityText.isBlank()) {
            return null;
        }

        String text = quantityText.trim().toLowerCase();

        if (text.contains("kg")) return IngredientUnit.KG;
        if (text.contains("ml")) return IngredientUnit.ML;
        if (text.contains("g")) return IngredientUnit.G;
        if (text.contains("l")) return IngredientUnit.L;
        if (text.contains("큰술")) return IngredientUnit.TBSP;
        if (text.contains("작은술")) return IngredientUnit.TSP;
        if (text.contains("컵")) return IngredientUnit.CUP;
        if (text.contains("개")) return IngredientUnit.PIECE;
        if (text.contains("약간")) return IngredientUnit.PINCH;

        return null;
    }
}
