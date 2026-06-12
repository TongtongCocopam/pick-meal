package kongju.pickmeal.core.menu.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum DishType {
    RICE("밥"),
    NOODLE("면"),
    SOUP("국/탕/찌개"),
    STEW("찜/조림/전골"),
    GRILL("구이"),
    STIR_FRY("볶음"),
    FRIED("튀김"),
    BRAISED("조림"),
    STEAMED("찜"),
    RAW("생식/회"),
    SALAD("샐러드"),
    SIDE_DISH("반찬"),
    MAIN_DISH("메인요리"),
    BREAD("빵"),
    DESSERT("디저트"),
    DRINK("음료"),
    SAUCE("소스/양념"),
    ETC("기타");

    private final String displayName;
}
