package kongju.pickmeal.application.menu;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.support.fixture.UserFixture;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.support.fixture.MenuFixture;
import kongju.pickmeal.core.menu.type.IngredientUnit;
import kongju.pickmeal.core.menu.type.IngredientType;
import kongju.pickmeal.application.menu.data.MenuDto;
import kongju.pickmeal.support.fixture.FamilyFixture;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.application.menu.data.FamilyCustomMenuDto;
import kongju.pickmeal.core.menu.repository.IngredientRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;


@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuIngredientRepository menuIngredientRepository;
    @Mock
    private UserReader userReader;
    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private MenuService menuService;

    @Nested
    @DisplayName("메뉴 검색")
    class MenuSearch {
        @Test
        @DisplayName("키워드 일치하는 메뉴가 없는경우")
        public void should_success_menu_search_when_keyword_not_found() {
            MenuCategory category = MenuCategory.KOREAN;
            DishType dishType = DishType.MAIN_DISH;
            String keyword = "없는요리";
            Pageable pageable = PageRequest.of(0, 1);

            given(menuRepository.searchByFilters(any(), any(), any(), any(Pageable.class))).willReturn(Page.empty(pageable));

            MenuDto.ListItemResponse response = menuService.searchMenus(category, dishType, keyword, pageable);
            assertEquals(List.of(), response.content());

        }
    }


    @Nested
    @DisplayName("메뉴 상세 정보")
    class MenuDetail {
        @Test
        @DisplayName("없는 메뉴")
        public void should_fail_detail_menu_not_found() {
            Long menuId = 1L;
            given(menuRepository.findById(menuId)).willThrow(new BusinessException(ErrorCode.INVALID_MENU_ID));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> menuService.detailMenu(menuId));

            assertEquals(ErrorCode.INVALID_MENU_ID, exception.getErrorCode());

        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_detail_menu() {
            Long menuId = 1L;
            Menu menu = MenuFixture.menu();

            given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));
            given(menuIngredientRepository.findAllByMenuWithIngredient(menu)).willReturn(List.of());

            MenuDto.DetailResponse response = menuService.detailMenu(menuId);

            assertEquals(menu.getMenuName(), response.menuName());
            assertEquals(List.of(), response.ingredients());

        }
    }

    @Nested
    @DisplayName("가족 메뉴 추가")
    class FamilyMenuCreate {
        @Test
        @DisplayName("가족이 없음")
        public void should_fail_create_family_menu_when_not_family_member() {
            Long userId = 1L;
            FamilyCustomMenuDto.SaveRequest request = FamilyCustomMenuDto.SaveRequest.builder().build();
            User user = UserFixture.user();

            given(userReader.getById(userId)).willReturn(user);
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> menuService.createMenu(userId, request));

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("재료id가 있지만 없는 재료")
        public void should_fail_create_family_menu_when_ingredient_not_found() {
            Long userId = 1L;

            FamilyCustomMenuDto.SaveRequest request =
                    FamilyCustomMenuDto.SaveRequest.builder()
                            .menuName("닭가슴살 김치볶음밥")
                            .dishType(DishType.MAIN_DISH)
                            .category(MenuCategory.KOREAN)
                            .kcal(BigDecimal.valueOf(520.0))
                            .carbs(BigDecimal.valueOf(65.0))
                            .protein(BigDecimal.valueOf(32.0))
                            .fat(BigDecimal.valueOf(14.0))
                            .sodium(BigDecimal.valueOf(850.0))
                            .ingredients(List.of(
                                    FamilyCustomMenuDto.IngredientRequest.builder()
                                            .ingredientId(1L)
                                            .ingredientName("소금")
                                            .quantity(BigDecimal.valueOf(3.0))
                                            .unit(IngredientUnit.G)
                                            .type(IngredientType.SEASONING)
                                            .build()
                            ))
                            .build();

            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);

            given(userReader.getById(userId)).willReturn(user);
            given(ingredientRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> menuService.createMenu(userId, request));

            assertEquals(ErrorCode.INGREDIENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_create_family_menu() {
            Long userId = 1L;

            FamilyCustomMenuDto.SaveRequest request =
                    FamilyCustomMenuDto.SaveRequest.builder()
                            .menuName("닭가슴살 김치볶음밥")
                            .dishType(DishType.MAIN_DISH)
                            .category(MenuCategory.KOREAN)
                            .kcal(BigDecimal.valueOf(520.0))
                            .carbs(BigDecimal.valueOf(65.0))
                            .protein(BigDecimal.valueOf(32.0))
                            .fat(BigDecimal.valueOf(14.0))
                            .sodium(BigDecimal.valueOf(850.0))
                            .ingredients(List.of(
                                    FamilyCustomMenuDto.IngredientRequest.builder()
                                            .ingredientId(1L)
                                            .ingredientName("소금")
                                            .quantity(BigDecimal.valueOf(3.0))
                                            .unit(IngredientUnit.G)
                                            .type(IngredientType.SEASONING)
                                            .build()
                            ))
                            .build();

            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);

            Ingredient ingredient = Ingredient.create("소금");

            given(userReader.getById(userId)).willReturn(user);
            given(ingredientRepository.findById(any())).willReturn(Optional.of(ingredient));

            menuService.createMenu(userId, request);

            verify(menuIngredientRepository, times(1)).saveAll(anyList());
        }
    }


    @Nested
    @DisplayName("가족 메뉴 삭제")
    class FamilyMenuDelete {
        @Test
        @DisplayName("가족이 없는 경우")
        public void should_fail_family_menu_delete_when_family_not_found() {
            Long userId = 1L;
            Long menuId = 1L;
            User user = UserFixture.user();

            given(userReader.getById(userId)).willReturn(user);
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> menuService.deleteCustomMenu(userId, menuId));

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("리더와 메뉴 가족이 일치하지 않는 경우")
        public void should_fail_family_menu_delete_when_menu_family_not_match() {
            Long userId = 1L;
            Long menuId = 1L;
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);
            given(userReader.getById(userId)).willReturn(user);

            Family family1 = FamilyFixture.family();
            Menu menu = Menu.createFamilyMenu(
                    "된장국",
                    MenuCategory.KOREAN,
                    DishType.MAIN_DISH,
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    family1);
            given(menuRepository.findById(menuId)).willReturn(Optional.ofNullable(menu));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> menuService.deleteCustomMenu(userId, menuId));

            assertEquals(ErrorCode.NOT_YOUR_FAMILY_REQUEST, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_family_menu_delete() {
            Long userId = 1L;
            Long menuId = 1L;
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);
            given(userReader.getById(userId)).willReturn(user);

            Menu menu = Menu.createFamilyMenu(
                    "된장국",
                    MenuCategory.KOREAN,
                    DishType.MAIN_DISH,
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    BigDecimal.valueOf(520.0),
                    family);
            given(menuRepository.findById(menuId)).willReturn(Optional.ofNullable(menu));
            menuService.deleteCustomMenu(userId, menuId);

            verify(menuIngredientRepository, times(1)).deleteAllByMenu(any());
            verify(menuRepository, times(1)).delete(any());

        }
    }
}
