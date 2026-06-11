package kongju.pickmeal.infrastructure.external.recipe.foodsafety;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import kongju.pickmeal.core.diet.type.MenuCategory;
import kongju.pickmeal.core.diet.type.IngredientType;
import kongju.pickmeal.infrastructure.external.recipe.parser.IngredientMenuParser;
import kongju.pickmeal.infrastructure.external.recipe.foodsafety.data.FoodSafetyRecipeRow;


@Component
@RequiredArgsConstructor
public class FoodMapper {
    private final IngredientMenuParser ingredientMenuParser;

    public Menu toMenu(FoodSafetyRecipeRow row) {
        return Menu.createDefaultMenu(
                null,
                row.menuName(),
                MenuCategory.KOREAN,
                ingredientMenuParser.mapDishType(row.dishType()),
                ingredientMenuParser.parseBigDecimal(row.kcal()),
                ingredientMenuParser.parseBigDecimal(row.carbs()),
                ingredientMenuParser.parseBigDecimal(row.protein()),
                ingredientMenuParser.parseBigDecimal(row.fat()),
                ingredientMenuParser.parseBigDecimal(row.sodium())
        );
    }

    public MenuIngredient toMenuIngredient(
            Menu menu,
            Ingredient ingredient,
            String quantityText
    ) {
        return MenuIngredient.create(
                menu,
                ingredient,
                quantityText,
                null,
                null,
                IngredientType.ETC
        );
    }
}
