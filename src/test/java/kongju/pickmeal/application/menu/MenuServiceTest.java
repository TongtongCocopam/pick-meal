package kongju.pickmeal.application.menu;

import java.util.List;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.support.fixture.MenuFixture;
import kongju.pickmeal.application.menu.data.MenuDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;


@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuIngredientRepository menuIngredientRepository;

    @InjectMocks
    private MenuService menuService;

    @Nested
    @DisplayName("메뉴 상세 정보")
    class MenuDetail{
        @Test
        @DisplayName("없는 메뉴")
        public void should_fail_detail_menu_not_found(){
            Long menuId = 1L;
            given(menuRepository.findById(menuId)).willThrow(new BusinessException(ErrorCode.INVALID_MENU_ID));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> menuService.detailMenu(menuId));

            assertEquals(ErrorCode.INVALID_MENU_ID, exception.getErrorCode());

        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_detail_menu(){
            Long menuId = 1L;
            Menu menu = MenuFixture.menu();

            given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));
            given(menuIngredientRepository.findAllByMenuWithIngredient(menu)).willReturn(List.of());

            MenuDto.DetailResponse response = menuService.detailMenu(menuId);

            assertEquals(menu.getMenuName(), response.menuName());
            assertEquals(List.of(), response.ingredients());

        }
    }

}
