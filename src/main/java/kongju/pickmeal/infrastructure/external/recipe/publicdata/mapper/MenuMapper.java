package kongju.pickmeal.infrastructure.external.recipe.publicdata.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.infrastructure.external.recipe.parser.IngredientMenuParser;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.data.info.RecipeInfoRow;


@Component
@RequiredArgsConstructor
public class MenuMapper {
    private final IngredientMenuParser ingredientMenuParser;

    /**
     * 메뉴 테이블에 넣을 수 있도록 변환
     * @param row 메뉴 정보
     * @return 메뉴 객체
     */
    public Menu toMenu(RecipeInfoRow row) {
        return Menu.createDefaultMenu(
                row.recipeId(),
                row.recipeName(),
                ingredientMenuParser.mapCategory(row.nationName()),
                ingredientMenuParser.mapDishType(row.typeName()),
                ingredientMenuParser.parseBigDecimal(row.kcal()),
                null,
                null,
                null,
                null
        );
    }

}
