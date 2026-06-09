package kongju.pickmeal.infrastructure.external.recipe.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.type.DishType;
import kongju.pickmeal.core.diet.type.MenuCategory;
import kongju.pickmeal.infrastructure.external.recipe.data.info.RecipeInfoRow;


@Component
public class MenuMapper {

    /**
     * 메뉴 테이블에 넣을 수 있도록 변환
     * @param row 메뉴 정보
     * @return 메뉴 객체
     */
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

    /**
     * 카테고리 양식에 맞게 enum타입으로 변환
     * @param nationName 카테고리 이름
     * @return 카테고리 enum
     */
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

    /**
     * 메뉴 종류에 맞게 enum타입으로 변환
     * 국, 탕 찌개 등
     * @param typeName 종류
     * @return 종류 enum타입
     */
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

    /**
     * 칼로리 정보
     * @param value 칼로리
     * @return db형태에 맞게 변환
     */
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
