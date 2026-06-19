package kongju.pickmeal.support.fixture;

import java.math.BigDecimal;
import java.util.List;

import kongju.pickmeal.application.menu.data.MenuDto;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;


public class MenuFixture {
    public static Menu menu(){
        return Menu.createDefaultMenu(
                null,
                "된장국",
                MenuCategory.KOREAN,
                DishType.STEW,
                BigDecimal.valueOf(45.2),
                BigDecimal.valueOf(5.3),
                BigDecimal.valueOf(3.1),
                BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(350.0)
                );
    }

    public static Menu menu(String menuName){
        return Menu.createDefaultMenu(
                null,
                menuName,
                MenuCategory.KOREAN,
                DishType.STEW,
                BigDecimal.valueOf(45.2),
                BigDecimal.valueOf(5.3),
                BigDecimal.valueOf(3.1),
                BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(350.0)
        );
    }

    public static MenuDto.DetailResponse menuDetailResponse(){
        return MenuDto.DetailResponse.builder()
                .menuId(1L)
                .menuName("된장국")
                .category(MenuCategory.KOREAN)
                .dishType(DishType.STEW)
                .kcal(BigDecimal.valueOf(45.2))
                .carbs(BigDecimal.valueOf(5.3))
                .protein(BigDecimal.valueOf(3.1))
                .fat(BigDecimal.valueOf(1.2))
                .sodium(BigDecimal.valueOf(350.0))
                .ingredients(List.of())
                .build();
    }
}
