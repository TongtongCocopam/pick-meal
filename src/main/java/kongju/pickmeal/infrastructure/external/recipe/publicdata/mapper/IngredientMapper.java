package kongju.pickmeal.infrastructure.external.recipe.publicdata.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.infrastructure.external.recipe.parser.IngredientMenuParser;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.data.ingredient.RecipeIngredientRow;


@Component
@RequiredArgsConstructor
public class IngredientMapper {
    private final IngredientMenuParser ingredientMenuParser;

    /**
     * 재료 이름 공백 제거와 null체크
     *
     * @param ingredientName 재료 이름
     * @return 재료 이름 string반환
     */
    public String normalizeIngredientName(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return null;
        }
        return ingredientName.trim();
    }

    /**
     * 메뉴 재료 연결 테이블에 넣을 형식으로 변환
     *
     * @param row        재료 정보
     * @param menu       메뉴 객체
     * @param ingredient 재료 객체
     * @return 메뉴 재료 객체 반환
     */
    public MenuIngredient toMenuIngredient(
            RecipeIngredientRow row,
            Menu menu,
            Ingredient ingredient

    ) {
        return MenuIngredient.create(
                menu,
                ingredient,
                row.quantityText(),
                ingredientMenuParser.parseQuantity(row.quantityText()),
                ingredientMenuParser.parseUnit(row.quantityText()),
                ingredientMenuParser.parseIngredientType(row.ingredientTypeName())
        );
    }


}
