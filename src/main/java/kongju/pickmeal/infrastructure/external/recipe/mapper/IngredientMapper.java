package kongju.pickmeal.infrastructure.external.recipe.mapper;

import org.springframework.stereotype.Component;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import kongju.pickmeal.core.diet.type.IngredientUnit;
import kongju.pickmeal.core.diet.type.IngredientType;
import kongju.pickmeal.infrastructure.external.recipe.data.ingredient.RecipeIngredientRow;


@Component
public class IngredientMapper {

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
                parseQuantity(row.quantityText()),
                parseUnit(row.quantityText()),
                parseIngredientType(row.ingredientTypeName())
        );
    }

    /**
     * 용량 단위 떼고 double로 변환
     *
     * @param quantityText 용량
     * @return 재료 용량 반환
     */
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

    /**
     * 용량 단위 뽑아내기
     *
     * @param quantityText 용량
     * @return 단위
     */
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

    /**
     * 주재료인지 부재료인지 판단
     * @param ingredientType 재료 타입
     * @return enum타입 반환
     */
    private IngredientType parseIngredientType(String ingredientType) {
        if (ingredientType == null || ingredientType.isBlank()) {
            return IngredientType.ETC;
        }

        String type = ingredientType.trim().toLowerCase();

        return switch (type) {
            case "주재료" -> IngredientType.MAIN;
            case "양념" -> IngredientType.SEASONING;
            case "부재료" -> IngredientType.SUB;
            default -> IngredientType.ETC;
        };
    }
}
