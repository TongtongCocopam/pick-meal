package kongju.pickmeal.core.diet;

import java.util.UUID;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.diet.type.UserMenuPickStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static kongju.pickmeal.support.fixture.MenuFixture.menu;
import static kongju.pickmeal.support.fixture.UserFixture.user;


public class UserMenuPickTest {
    @Test
    @DisplayName("유저가 선택한 메뉴 사용")
    public void menu_used(){
        User user = user();
        Menu menu = menu();
        UserMenuPick userMenuPick = UserMenuPick.create(
                user,
                menu,
                LocalDate.now(),
                UUID.randomUUID()
        );

        userMenuPick.used();
        assertThat(userMenuPick.getStatus()).isEqualTo(UserMenuPickStatus.USED);
    }

    @Test
    @DisplayName("유저 선택한 메뉴 롤백")
    public void menu_rollback_used(){
        User user = user();
        Menu menu = menu();
        UserMenuPick userMenuPick = UserMenuPick.create(
                user,
                menu,
                LocalDate.now(),
                UUID.randomUUID()
        );

        userMenuPick.rollbackUse();
        assertThat(userMenuPick.getStatus()).isEqualTo(UserMenuPickStatus.PENDING);
    }
}
