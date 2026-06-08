package kongju.pickmeal.infrastructure.external.recipe.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.type.DishType;
import kongju.pickmeal.core.diet.type.MenuCategory;
import kongju.pickmeal.infrastructure.external.recipe.data.info.RecipeInfoRow;


@Component
public class MenuMapper {

    public Menu toMenu(RecipeInfoRow row) {
        return Menu.createDefaultMenu(
                row.recipeId(),
                row.recipeName(),
                mapCategory(row.nationName()),
                mapDishType(row.typeName()),
                parseBigDecimal(row.kcal()),
                null,
                null,
                null,
                null
        );
    }

    private MenuCategory mapCategory(String nationName) {
        if ("한식".equals(nationName)) {
            return MenuCategory.KOREAN;
        }
        if ("중식".equals(nationName) || "중국".equals(nationName)) {
            return MenuCategory.CHINESE;
        }
        if ("일식".equals(nationName) || "일본".equals(nationName)) {
            return MenuCategory.JAPANESE;
        }
        if ("양식".equals(nationName) || "서양".equals(nationName)) {
            return MenuCategory.WESTERN;
        }
        return MenuCategory.ETC;
    }

    private DishType mapDishType(String typeName) {
        if ("밥".equals(typeName)) {
            return DishType.RICE;
        }
        if ("국&찌개".equals(typeName) || "국".equals(typeName) || "찌개".equals(typeName)) {
            return DishType.SOUP;
        }
        if ("반찬".equals(typeName)) {
            return DishType.SIDE_DISH;
        }
        if ("후식".equals(typeName)) {
            return DishType.DESSERT;
        }
        return DishType.ETC;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
