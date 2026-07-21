package kongju.pickmeal.infrastructure.external.recipe.parser;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.core.menu.type.IngredientType;
import kongju.pickmeal.core.menu.type.IngredientUnit;


@Component
public class IngredientMenuParser {
    private static final int MAX_INGREDIENT_NAME_LENGTH = 100;

    /**
     * 용량 단위 떼고 double로 변환
     *
     * @param quantityText 용량
     * @return 재료 용량 반환
     */
    public BigDecimal parseQuantity(String quantityText) {
        if (quantityText == null || quantityText.isBlank()) {
            return null;
        }
        String text = quantityText.split("\\(")[0].trim();

        // "20g", "200ml" 같은 단순 케이스만 우선 처리
        if (text.contains("/")) {
            return null;
        }

        String number = text.replaceAll("[^0-9.]", "");

        if (number.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(number);
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
    public IngredientUnit parseUnit(String quantityText) {
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
    public IngredientType parseIngredientType(String ingredientType) {
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


    /**
     * 카테고리 양식에 맞게 enum타입으로 변환
     * @param nationName 카테고리 이름
     * @return 카테고리 enum
     */
    public MenuCategory mapCategory(String nationName) {
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
    public DishType mapDishType(String typeName) {
        if ("밥".equals(typeName)) {
            return DishType.RICE;
        }
        if ("국&찌개".equals(typeName) || "국".equals(typeName) || "찌개".equals(typeName) || "끓이기".equals(typeName) || "찌개/전골/스튜".equals(typeName)) {
            return DishType.SOUP;
        }
        if("찜".equals(typeName)) {
            return DishType.STEW;
        }
        if("볶음".equals(typeName)) {
            return DishType.STIR_FRY;
        }
        if ("반찬".equals(typeName) || "부침".equals(typeName)) {
            return DishType.SIDE_DISH;
        }
        if ("후식".equals(typeName) || "빵/과자".equals(typeName)) {
            return DishType.DESSERT;
        }
        if("샌드위치/햄버거".equals(typeName) || "양식".equals(typeName)) {
            return DishType.MAIN_DISH;
        }
        if("튀김/커틀릿".equals(typeName)) {
            return DishType.FRIED;
        }
        if("양념장".equals(typeName)) {
            return DishType.SAUCE;
        }
        if("조림".equals(typeName)) {
            return DishType.BRAISED;
        }
        return DishType.ETC;
    }

    /**
     * 테이블 양식에 맞춰 형 변환
     * @param value 변환할 값
     * @return 소숫점 1의 자리까지 변환
     */
    public BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim())
                    .setScale(1, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 긴 String에서 재료와 양 추출
     * @param ingredientText 재료
     * @return 파싱한 데이터
     */
    public List<ParsedIngredient> extractIngredientParts(String ingredientText) {
        if (ingredientText == null || ingredientText.isBlank()) {
            return List.of();
        }

        List<ParsedIngredient> result = new ArrayList<>();

        for (String line : ingredientText.split("\\R")) {
            String ingredientLine = extractIngredientLine(line);

            if (ingredientLine == null) {
                continue;
            }

            for (String part : ingredientLine.split(",")) {
                ParsedIngredient parsed = parseIngredientPart(part);

                if (parsed != null) {
                    result.add(parsed);
                }
            }
        }

        return result;
    }

    /**
     * 필요없는 설명이나 이름 분리
     * @param line 분리할 라인
     * @return 파싱 후 값
     */
    private String extractIngredientLine(String line) {
        String normalized = normalize(line);

        if (normalized.isBlank()) {
            return null;
        }

        if (!normalized.contains(":")) {
            return normalized;
        }

        String[] parts = normalized.split(":", 2);

        if (parts.length < 2 || parts[1].isBlank()) {
            return null;
        }

        return parts[1].trim();
    }

    /**
     * 재료와 양 분리
     * @param part 하나의 String
     * @return 재료, 양 객체
     */
    private ParsedIngredient parseIngredientPart(String part) {
        String value = normalize(part);

        if (value.isBlank()) {
            return null;
        }

        int quantityStartIndex = findQuantityStartIndex(value);

        if (quantityStartIndex >= 0) {
            String ingredientName = value.substring(0, quantityStartIndex).trim();
            String quantityText = value.substring(quantityStartIndex).trim();

            return buildParsedIngredient(ingredientName, quantityText);
        }

        return parseWordQuantity(value);
    }

    /**
     * 양 분리
     * @param value String
     * @return 특정 수량 지칭 단어 분리한 값
     */
    private ParsedIngredient parseWordQuantity(String value) {
        for (String quantityWord : List.of("약간", "적당량")) {
            if (value.endsWith(quantityWord)) {
                String ingredientName = value.substring(0, value.length() - quantityWord.length()).trim();

                return buildParsedIngredient(ingredientName, quantityWord);
            }
        }

        return null;
    }

    /**
     * 최대 길이 확인과 분리
     * @param ingredientName 재료 이름
     * @param quantityText 재료 양
     * @return 재료 정보 객체
     */
    private ParsedIngredient buildParsedIngredient(String ingredientName, String quantityText) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return null;
        }

        if (ingredientName.length() > MAX_INGREDIENT_NAME_LENGTH) {
            return null;
        }

        if (quantityText == null || quantityText.isBlank()) {
            return null;
        }

        return ParsedIngredient.builder()
                .ingredientName(ingredientName)
                .quantityText(quantityText)
                .build();
    }

    /**
     * 숫자 인덱스만 계산
     * @param value 양 정보
     * @return 인덱스 길이
     */
    private int findQuantityStartIndex(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 필요없는 문자열 제거
     * @param value 재료 text
     * @return 전처리한 문자열
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace("●", "")
                .replace("·", "")
                .trim();
    }
}
