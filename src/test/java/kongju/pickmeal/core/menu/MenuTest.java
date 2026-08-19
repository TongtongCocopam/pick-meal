package kongju.pickmeal.core.menu;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;

import static kongju.pickmeal.support.fixture.MenuFixture.menu;


public class MenuTest {
    @Test
    @DisplayName("메뉴 업데이트")
    void menu_update(){
        Menu menu = menu();

        menu.update(
                "황태해장국",
                MenuCategory.ASIAN,
                DishType.SOUP,
                BigDecimal.valueOf(350),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(20)
        );

        assertThat(menu.getMenuName()).isEqualTo("황태해장국");
        assertThat(menu.getCategory()).isEqualTo(MenuCategory.ASIAN);
        assertThat(menu.getDishType()).isEqualTo(DishType.SOUP);
    }
}
