package kongju.pickmeal.application.menu;

import java.util.*;
import java.math.BigDecimal;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.core.menu.type.IngredientUnit;
import kongju.pickmeal.core.menu.type.IngredientType;
import kongju.pickmeal.application.menu.data.MenuDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.menu.data.MenuFilterDto;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.application.menu.data.FamilyCustomMenuDto;
import kongju.pickmeal.core.menu.repository.IngredientRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;

import static kongju.pickmeal.support.fixture.UserFixture.user;
import static kongju.pickmeal.support.fixture.MenuFixture.menu;
import static kongju.pickmeal.support.fixture.FamilyFixture.family;


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
            Menu menu = menu();

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
            User user = user();

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

            User user = user();
            Family family = family();
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

            User user = user();
            Family family = family();
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
            User user = user();

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
            User user = user();
            Family family = family();
            user.joinFamilyLeader(family);
            given(userReader.getById(userId)).willReturn(user);

            Family family1 = family();
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
            User user = user();
            Family family = family();
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

    @Nested
    @DisplayName("필터링 카테고리 목록 가져오기")
    class FilterMetadata {
        @Test
        @DisplayName("카테고리 목록 가져오기 성공")
        void should_success_get_category_when_get_filter_metadata() {
            MenuFilterDto.MetadataResponse response = menuService.getFilterMetadata();

            assertThat(response.categories())
                    .extracting(MenuFilterDto.CategoryResponse::name)
                    .containsExactlyInAnyOrder(MenuCategory.values());

            assertThat(response.dishTypes())
                    .extracting(MenuFilterDto.DishTypeResponse::name)
                    .containsExactlyInAnyOrder(DishType.values());
        }
    }

    @Nested
    @DisplayName("메뉴 찾기")
    class SearchMenu {
        @Test
        @DisplayName("검색된 카테고리나 메뉴가 없는 경우")
        void should_return_empty_page_when_no_menu_found() {
            Pageable pageable = PageRequest.of(0, 10);

            given(menuRepository.searchByFilters(null, null, "", pageable))
                    .willReturn(Page.empty(pageable));

            MenuDto.ListItemResponse response = menuService.searchMenus(null, null, null, pageable);

            assertThat(response.content()).isEmpty();
            assertThat(response.pageInfo().currentPage()).isEqualTo(1);
            assertThat(response.pageInfo().totalPages()).isZero();
            assertThat(response.pageInfo().totalElements()).isZero();
        }

        @Test
        @DisplayName("검색된 카테고리나 메뉴가 없는 경우")
        void should_return_item_page_when_menu_found() {
            Pageable pageable = PageRequest.of(0, 10);

            Menu menu1 = menu();
            Menu menu2 = menu("카레");
            List<Menu> menus = List.of(menu1, menu2);

            Page<Menu> page = new PageImpl<>(menus, pageable, menus.size());

            given(menuRepository.searchByFilters(any(), any(), any(), any()))
                    .willReturn(page);

            MenuDto.ListItemResponse response = menuService.searchMenus(null, null, null, pageable);

            assertThat(response.content()).hasSize(2);
            assertThat(response.pageInfo().currentPage()).isEqualTo(1);
            assertThat(response.pageInfo().totalPages()).isEqualTo(1);
            assertThat(response.pageInfo().totalElements()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("메뉴 상세 정보")
    class DetailMenu {
        @Test
        @DisplayName("메뉴가 없는 경우")
        void should_fail_found_menu_when_menu_not_found() {
            given(menuRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                menuService.detailMenu(any());
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MENU_ID);
        }

        @Test
        @DisplayName("재료가 없는 경우")
        void should_fail_search_ingredients_when_ingredient_not_found() {
            Menu menu = menu();
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));
            given(menuIngredientRepository.findAllByMenuWithIngredient(any())).willReturn(List.of());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                menuService.detailMenu(any());
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INGREDIENT_NOT_FOUND);
        }


        @Test
        @DisplayName("성공 케이스")
        void should_success_detail_menu() {
            Menu menu = menu();
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            List<MenuIngredient> ingredients = menuIngredientsCreate();

            given(menuIngredientRepository.findAllByMenuWithIngredient(any())).willReturn(ingredients);

            MenuDto.DetailResponse response = menuService.detailMenu(any());
            assertThat(response.ingredients()).hasSize(2);
            assertThat(response.ingredients().getFirst().ingredientName()).isEqualTo("계란");

        }

        List<MenuIngredient> menuIngredientsCreate() {
            MenuIngredient menuIngredient1 = mock(MenuIngredient.class);
            MenuIngredient menuIngredient2 = mock(MenuIngredient.class);
            Ingredient ingredient1 = mock(Ingredient.class);
            Ingredient ingredient2 = mock(Ingredient.class);
            given(ingredient1.getName()).willReturn("계란");
            given(ingredient2.getName()).willReturn("설탕");
            given(menuIngredient1.getIngredient()).willReturn(ingredient1);
            given(menuIngredient1.getQuantityText()).willReturn("1개");
            given(menuIngredient2.getIngredient()).willReturn(ingredient2);
            given(menuIngredient2.getQuantityText()).willReturn("30g");

            return List.of(menuIngredient1, menuIngredient2);
        }
    }

    @Nested
    @DisplayName("가족 메뉴 생성")
    class CreateMenu {
        @Test
        @DisplayName("가족을 찾지 못한 경우")
        void should_fail_when_family_not_found() {
            Long userId = 1L;
            User user = user();

            FamilyCustomMenuDto.SaveRequest request = FamilyCustomMenuDto.SaveRequest.builder()
                    .build();

            given(userReader.getById(userId)).willReturn(user);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    menuService.createMenu(userId, request)
            );

            assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());

        }

        @Test
        @DisplayName("재료 아이디를 찾지 못한 경우")
        void should_fail_create_menu_when_not_found_ingredient() {
            Long userId = 1L;
            Family family = family();
            User user = user();
            user.joinFamilyLeader(family);

            List<FamilyCustomMenuDto.IngredientRequest> ingredientRequests = List.of(
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientId(1L)
                            .ingredientName("계란")
                            .quantity(BigDecimal.valueOf(1))
                            .unit(IngredientUnit.PIECE)
                            .type(IngredientType.MAIN)
                            .build(),
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientId(2L)
                            .ingredientName("파")
                            .quantity(BigDecimal.ONE)
                            .unit(IngredientUnit.G)
                            .type(IngredientType.SUB)
                            .build()
            );

            FamilyCustomMenuDto.SaveRequest request = FamilyCustomMenuDto.SaveRequest.builder()
                    .menuName("계란국")
                    .dishType(DishType.SOUP)
                    .category(MenuCategory.ASIAN)
                    .kcal(BigDecimal.valueOf(230))
                    .carbs(BigDecimal.valueOf(230))
                    .protein(BigDecimal.valueOf(230))
                    .fat(BigDecimal.valueOf(230))
                    .sodium(BigDecimal.valueOf(230))
                    .ingredients(ingredientRequests)
                    .build();

            given(userReader.getById(userId)).willReturn(user);

            given(menuRepository.save(any(Menu.class))).willAnswer(invocation -> invocation.getArgument(0));

            given(ingredientRepository.findById(1L)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    menuService.createMenu(userId, request)
            );

            assertEquals(ErrorCode.INGREDIENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("재료 이름이 비어있음")
        void should_fail_create_menu_when_ingredient_name_is_null() {
            Long userId = 1L;
            Family family = family();
            User user = user();
            user.joinFamilyLeader(family);

            List<FamilyCustomMenuDto.IngredientRequest> ingredientRequests = List.of(
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientId(1L)
                            .ingredientName("계란")
                            .quantity(BigDecimal.valueOf(1))
                            .unit(IngredientUnit.PIECE)
                            .type(IngredientType.MAIN)
                            .build(),
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientName("")
                            .quantity(BigDecimal.ONE)
                            .unit(IngredientUnit.G)
                            .type(IngredientType.SUB)
                            .build()
            );
            FamilyCustomMenuDto.SaveRequest request = FamilyCustomMenuDto.SaveRequest.builder()
                    .menuName("계란국")
                    .dishType(DishType.SOUP)
                    .category(MenuCategory.ASIAN)
                    .kcal(BigDecimal.valueOf(230))
                    .carbs(BigDecimal.valueOf(230))
                    .protein(BigDecimal.valueOf(230))
                    .fat(BigDecimal.valueOf(230))
                    .sodium(BigDecimal.valueOf(230))
                    .ingredients(ingredientRequests)
                    .build();

            given(userReader.getById(userId)).willReturn(user);
            given(menuRepository.save(any(Menu.class))).willAnswer(invocation -> invocation.getArgument(0));

            Ingredient ingredient = Ingredient.create("계란");
            given(ingredientRepository.findById(1L)).willReturn(Optional.of(ingredient));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    menuService.createMenu(userId, request)
            );

            assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        }

        @Test
        @DisplayName("재료 ID가 없고 같은 이름의 재료가 존재하면 기존 재료 사용")
        void should_use_existing_ingredient_when_found_by_name() {
            Long userId = 1L;
            Family family = family();
            User user = user();
            user.joinFamilyLeader(family);

            FamilyCustomMenuDto.IngredientRequest ingredientRequest =
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientName("  계란  ")
                            .quantity(BigDecimal.ONE)
                            .unit(IngredientUnit.PIECE)
                            .type(IngredientType.MAIN)
                            .build();

            FamilyCustomMenuDto.SaveRequest request =
                    FamilyCustomMenuDto.SaveRequest.builder()
                            .menuName("계란국")
                            .dishType(DishType.SOUP)
                            .category(MenuCategory.ASIAN)
                            .kcal(BigDecimal.valueOf(230))
                            .carbs(BigDecimal.valueOf(230))
                            .protein(BigDecimal.valueOf(230))
                            .fat(BigDecimal.valueOf(230))
                            .sodium(BigDecimal.valueOf(230))
                            .ingredients(List.of(ingredientRequest))
                            .build();

            Ingredient existingIngredient = Ingredient.create("계란");

            given(userReader.getById(userId)).willReturn(user);
            given(menuRepository.save(any(Menu.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(ingredientRepository.findByName("계란"))
                    .willReturn(Optional.of(existingIngredient));

            menuService.createMenu(userId, request);

            then(ingredientRepository).should().findByName("계란");
            then(ingredientRepository).should(never()).save(any(Ingredient.class));
            then(menuIngredientRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("재료 ID가 없고 같은 이름의 재료도 없으면 새 재료 생성")
        void should_create_ingredient_when_not_found_by_name() {
            Long userId = 1L;
            Family family = family();
            User user = user();
            user.joinFamilyLeader(family);

            FamilyCustomMenuDto.IngredientRequest ingredientRequest =
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientName("대파")
                            .quantity(BigDecimal.ONE)
                            .unit(IngredientUnit.G)
                            .type(IngredientType.SUB)
                            .build();

            FamilyCustomMenuDto.SaveRequest request =
                    FamilyCustomMenuDto.SaveRequest.builder()
                            .menuName("계란국")
                            .dishType(DishType.SOUP)
                            .category(MenuCategory.ASIAN)
                            .kcal(BigDecimal.valueOf(230))
                            .carbs(BigDecimal.valueOf(230))
                            .protein(BigDecimal.valueOf(230))
                            .fat(BigDecimal.valueOf(230))
                            .sodium(BigDecimal.valueOf(230))
                            .ingredients(List.of(ingredientRequest))
                            .build();

            given(userReader.getById(userId)).willReturn(user);
            given(menuRepository.save(any(Menu.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(ingredientRepository.findByName("대파")).willReturn(Optional.empty());
            given(ingredientRepository.save(any(Ingredient.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            menuService.createMenu(userId, request);

            then(ingredientRepository).should().findByName("대파");
            then(ingredientRepository).should().save(any(Ingredient.class));
            then(menuIngredientRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("성공 케이스")
        void should_success_create_menu() {
            Long userId = 1L;
            Family family = family();
            User user = user();
            user.joinFamilyLeader(family);

            List<FamilyCustomMenuDto.IngredientRequest> ingredientRequests = List.of(
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientId(1L)
                            .ingredientName("계란")
                            .quantity(BigDecimal.valueOf(1))
                            .unit(IngredientUnit.PIECE)
                            .type(IngredientType.MAIN)
                            .build(),
                    FamilyCustomMenuDto.IngredientRequest.builder()
                            .ingredientId(2L)
                            .ingredientName("파")
                            .quantity(BigDecimal.ONE)
                            .unit(IngredientUnit.G)
                            .type(IngredientType.SUB)
                            .build()
            );
            FamilyCustomMenuDto.SaveRequest request = FamilyCustomMenuDto.SaveRequest.builder()
                    .menuName("계란국")
                    .dishType(DishType.SOUP)
                    .category(MenuCategory.ASIAN)
                    .kcal(BigDecimal.valueOf(230))
                    .carbs(BigDecimal.valueOf(230))
                    .protein(BigDecimal.valueOf(230))
                    .fat(BigDecimal.valueOf(230))
                    .sodium(BigDecimal.valueOf(230))
                    .ingredients(ingredientRequests)
                    .build();

            given(userReader.getById(userId)).willReturn(user);
            given(menuRepository.save(any(Menu.class))).willAnswer(invocation -> invocation.getArgument(0));

            Ingredient ingredient = Ingredient.create("계란");
            Ingredient ingredient2 = Ingredient.create("파");
            given(ingredientRepository.findById(1L)).willReturn(Optional.of(ingredient));
            given(ingredientRepository.findById(2L)).willReturn(Optional.of(ingredient2));

            menuService.createMenu(userId, request);
            then(menuRepository).should().save(any(Menu.class));
        }
    }

    @Nested
    @DisplayName("유저 메뉴 수정")
    class CustomMenuUpdate {
        @Test
        @DisplayName("메뉴를 찾지 못하면 수정 실패")
        void should_fail_update_when_menu_not_found() {
            Long userId = 1L;
            Long menuId = 999L;

            User user = user();

            given(userReader.getById(userId)).willReturn(user);
            given(menuRepository.findById(menuId)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> menuService.updateCustomMenu(
                            userId, menuId,
                            FamilyCustomMenuDto.SaveRequest.builder().build()
                    )
            );

            assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
            then(menuIngredientRepository).shouldHaveNoInteractions();
        }
    }

    @Test
    @DisplayName("사용자가 가족에 속하지 않으면 수정 실패")
    void should_fail_update_when_family_not_found() {
        Long userId = 1L;
        Long menuId = 1L;

        User user = user();
        Family family = family();

        Menu menu = Menu.createFamilyMenu(
                "계란국",
                MenuCategory.KOREAN,
                DishType.SOUP,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                family
        );

        given(userReader.getById(userId)).willReturn(user);
        given(menuRepository.findById(menuId))
                .willReturn(Optional.of(menu));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> menuService.updateCustomMenu(
                        userId, menuId,
                        FamilyCustomMenuDto.SaveRequest.builder().build()
                )
        );

        assertEquals(ErrorCode.FAMILY_NOT_FOUND, exception.getErrorCode());
        then(menuIngredientRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("다른 가족의 메뉴는 수정할 수 없다")
    void should_fail_update_when_not_my_family_menu() {
        Long userId = 1L;
        Long menuId = 1L;

        Family myFamily = family();
        Family otherFamily = family("냠냠");

        User user = user();
        user.joinFamilyLeader(myFamily);

        Menu menu = Menu.createFamilyMenu(
                "계란국",
                MenuCategory.KOREAN,
                DishType.SOUP,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                otherFamily
        );

        given(userReader.getById(userId)).willReturn(user);
        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> menuService.updateCustomMenu(
                        userId, menuId,
                        FamilyCustomMenuDto.SaveRequest.builder().build()
                )
        );

        assertEquals(
                ErrorCode.NOT_YOUR_FAMILY_REQUEST,
                exception.getErrorCode()
        );
        then(menuIngredientRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("수정할 재료를 찾지 못하면 실패")
    void should_fail_update_when_ingredient_not_found() {
        Long userId = 1L;
        Long menuId = 1L;

        Family family = family();

        User user = user();
        user.joinFamilyLeader(family);

        Menu menu = Menu.createFamilyMenu(
                "기존 메뉴",
                MenuCategory.KOREAN,
                DishType.SOUP,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                family
        );

        FamilyCustomMenuDto.IngredientRequest ingredientRequest =
                FamilyCustomMenuDto.IngredientRequest.builder()
                        .ingredientId(99L)
                        .quantity(BigDecimal.ONE)
                        .unit(IngredientUnit.PIECE)
                        .type(IngredientType.MAIN)
                        .build();

        FamilyCustomMenuDto.SaveRequest request =
                FamilyCustomMenuDto.SaveRequest.builder()
                        .menuName("수정 메뉴")
                        .category(MenuCategory.ASIAN)
                        .dishType(DishType.SOUP)
                        .kcal(BigDecimal.ONE)
                        .carbs(BigDecimal.ONE)
                        .protein(BigDecimal.ONE)
                        .fat(BigDecimal.ONE)
                        .sodium(BigDecimal.ONE)
                        .ingredients(List.of(ingredientRequest))
                        .build();

        given(userReader.getById(userId)).willReturn(user);
        given(menuRepository.findById(menuId))
                .willReturn(Optional.of(menu));

        given(ingredientRepository.findById(99L))
                .willReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> menuService.updateCustomMenu(userId, menuId, request)
        );

        assertEquals(
                ErrorCode.INGREDIENT_NOT_FOUND,
                exception.getErrorCode()
        );

        then(menuIngredientRepository).should().deleteAllByMenu(menu);
        then(menuIngredientRepository).should().flush();
        then(menuIngredientRepository).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("메뉴 수정 성공")
    void should_success_update_custom_menu() {
        Long userId = 1L;
        Long menuId = 1L;

        Family family = family();

        User user = user();
        user.joinFamilyLeader(family);

        Menu menu = Menu.createFamilyMenu(
                "기존 메뉴",
                MenuCategory.KOREAN,
                DishType.SOUP,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                family
        );

        Ingredient ingredient = Ingredient.create("계란");

        List<FamilyCustomMenuDto.IngredientRequest> ingredientRequests = List.of(
                FamilyCustomMenuDto.IngredientRequest.builder()
                        .ingredientId(1L)
                        .quantity(BigDecimal.ONE)
                        .unit(IngredientUnit.PIECE)
                        .type(IngredientType.MAIN)
                        .build()
        );

        FamilyCustomMenuDto.SaveRequest request =
                FamilyCustomMenuDto.SaveRequest.builder()
                        .menuName("수정된 메뉴")
                        .category(MenuCategory.ASIAN)
                        .dishType(DishType.MAIN_DISH)
                        .kcal(BigDecimal.valueOf(300))
                        .carbs(BigDecimal.valueOf(20))
                        .protein(BigDecimal.valueOf(30))
                        .fat(BigDecimal.valueOf(15))
                        .sodium(BigDecimal.valueOf(500))
                        .ingredients(ingredientRequests)
                        .build();

        given(userReader.getById(userId)).willReturn(user);
        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));
        given(ingredientRepository.findById(1L)).willReturn(Optional.of(ingredient));

        menuService.updateCustomMenu(userId, menuId, request);

        assertThat(menu.getMenuName()).isEqualTo("수정된 메뉴");
        assertThat(menu.getCategory()).isEqualTo(MenuCategory.ASIAN);
        assertThat(menu.getDishType()).isEqualTo(DishType.MAIN_DISH);

        then(menuIngredientRepository).should().deleteAllByMenu(menu);
        then(menuIngredientRepository).should().flush();
        then(menuIngredientRepository).should().saveAll(anyList());
    }
}
