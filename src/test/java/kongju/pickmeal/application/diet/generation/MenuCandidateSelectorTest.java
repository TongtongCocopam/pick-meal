package kongju.pickmeal.application.diet.generation;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.assertj.core.api.Assertions.assertThat;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;


@ExtendWith(MockitoExtension.class)
public class MenuCandidateSelectorTest {
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuIngredientRepository menuIngredientRepository;
    @InjectMocks
    private MenuCandidateSelector menuCandidateSelector;


    @ParameterizedTest
    @CsvSource({
            "1, 60, 120",
            "2, 60, 120",
            "3, 90, 180"
    })
    @DisplayName("식사 횟수에 따라 필요한 후보 개수를 선택")
    void should_select_candidate_count_by_daily_meal_count(
            int dailyMealCount,
            int expectedSoupCount,
            int expectedSideDishCount
    ) {
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 30);

        List<Menu> menus = new ArrayList<>();

        menus.addAll(createMenus(1L, 100, DishType.SOUP));
        menus.addAll(createMenus(1001L, 200, DishType.SIDE_DISH));

        when(menuRepository.findAll()).thenReturn(menus);
        when(menuIngredientRepository.findAllByMenuInFetchIngredient(anyList())).thenReturn(List.of());

        List<AiDietGenerateDto.MenuCandidate> result =
                menuCandidateSelector.select(
                        dailyMealCount,
                        startDate,
                        endDate,
                        List.of(),
                        Set.of(),
                        Set.of()
                );

        long soupCount = result.stream()
                .filter(candidate ->
                        candidate.dishType() == DishType.SOUP)
                .count();

        long sideDishCount = result.stream()
                .filter(candidate ->
                        candidate.dishType() == DishType.SIDE_DISH)
                .count();

        assertThat(soupCount).isEqualTo(expectedSoupCount);
        assertThat(sideDishCount).isEqualTo(expectedSideDishCount);
        assertThat(result)
                .extracting(AiDietGenerateDto.MenuCandidate::menuId)
                .doesNotHaveDuplicates();
    }


    @Test
    @DisplayName("선호 후보가 부족하면 일반 메뉴로 부족한 후보를 보충")
    void should_fill_candidates_with_fallback_when_preferred_is_insufficient() {
        Ingredient preferredIngredient = mock(Ingredient.class);

        Menu preferredMenu = menu(1L, "두부국", DishType.SOUP);

        Menu fallbackMenu = menu(2L, "미역국", DishType.SOUP);

        MenuIngredient preferredMenuIngredient = menuIngredient(
                preferredMenu,
                preferredIngredient
        );

        when(preferredIngredient.getId()).thenReturn(100L);
        when(preferredIngredient.getName()).thenReturn("두부");

        when(menuIngredientRepository.findAllByIngredientWithMenu(preferredIngredient))
                .thenReturn(List.of(preferredMenuIngredient));

        when(menuRepository.findAll()).thenReturn(List.of(
                preferredMenu,
                fallbackMenu
        ));

        when(menuIngredientRepository.findAllByMenuInFetchIngredient(anyList()))
                .thenReturn(List.of(preferredMenuIngredient));

        List<AiDietGenerateDto.MenuCandidate> result =
                menuCandidateSelector.select(
                        1,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 1),
                        List.of(preferredIngredient),
                        Set.of(),
                        Set.of()
                );
        assertThat(result)
                .extracting(AiDietGenerateDto.MenuCandidate::menuId)
                .containsExactlyInAnyOrder(
                        1L,
                        2L
                );

        assertThat(result)
                .extracting(AiDietGenerateDto.MenuCandidate::menuId)
                .doesNotHaveDuplicates();
    }


    @Test
    @DisplayName("알레르기와 fallback 제외 재료가 포함된 일반 메뉴는 후보에서 제외")
    void should_exclude_fallback_menus_with_excluded_ingredients() {
        Ingredient allergyIngredient = mock(Ingredient.class);
        Ingredient dislikedIngredient = mock(Ingredient.class);
        Ingredient safeIngredient = mock(Ingredient.class);

        when(allergyIngredient.getId()).thenReturn(100L);
        when(dislikedIngredient.getId()).thenReturn(200L);

        when(safeIngredient.getId()).thenReturn(300L);
        when(safeIngredient.getName()).thenReturn("두부");


        Menu allergyMenu = mock(Menu.class);
        Menu dislikedMenu = mock(Menu.class);
        Menu safeMenu = mock(Menu.class);

        when(allergyMenu.getId()).thenReturn(1L);
        when(dislikedMenu.getId()).thenReturn(2L);

        when(safeMenu.getId()).thenReturn(3L);
        when(safeMenu.getMenuName()).thenReturn("두부조림");
        when(safeMenu.getDishType()).thenReturn(DishType.SIDE_DISH);


        MenuIngredient allergyMenuIngredient = mock(MenuIngredient.class);
        MenuIngredient dislikedMenuIngredient = mock(MenuIngredient.class);
        MenuIngredient safeMenuIngredient = mock(MenuIngredient.class);

        when(allergyMenuIngredient.getMenu()).thenReturn(allergyMenu);
        when(allergyMenuIngredient.getIngredient()).thenReturn(allergyIngredient);

        when(dislikedMenuIngredient.getMenu()).thenReturn(dislikedMenu);
        when(dislikedMenuIngredient.getIngredient()).thenReturn(dislikedIngredient);

        when(safeMenuIngredient.getMenu()).thenReturn(safeMenu);
        when(safeMenuIngredient.getIngredient()).thenReturn(safeIngredient);


        when(menuRepository.findAll()).thenReturn(List.of(
                allergyMenu,
                dislikedMenu,
                safeMenu
        ));

        when(menuIngredientRepository.findAllByMenuInFetchIngredient(anyList()))
                .thenReturn(List.of(
                        allergyMenuIngredient,
                        dislikedMenuIngredient,
                        safeMenuIngredient
                ));

        List<AiDietGenerateDto.MenuCandidate> result = menuCandidateSelector.select(
                1,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of(),
                Set.of(100L),
                Set.of(100L, 200L)
        );

        assertThat(result)
                .extracting(AiDietGenerateDto.MenuCandidate::menuId)
                .containsExactly(3L);
    }


    @Test
    @DisplayName("알레르기 재료가 포함된 선호 메뉴도 후보에서 제외")
    void should_exclude_preferred_menu_with_allergy() {
        Ingredient allergyIngredient = mock(Ingredient.class);
        Menu menu = mock(Menu.class);
        MenuIngredient menuIngredient = mock(MenuIngredient.class);

        when(allergyIngredient.getId()).thenReturn(100L);

        when(menu.getId()).thenReturn(1L);

        when(menuIngredient.getMenu()).thenReturn(menu);
        when(menuIngredient.getIngredient()).thenReturn(allergyIngredient);

        when(menuIngredientRepository.findAllByIngredientWithMenu(allergyIngredient)).thenReturn(List.of(menuIngredient));

        when(menuRepository.findAll()).thenReturn(List.of(menu));

        when(menuIngredientRepository.findAllByMenuInFetchIngredient(anyList())).thenReturn(List.of(menuIngredient));

        List<AiDietGenerateDto.MenuCandidate> result = menuCandidateSelector.select(
                1,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of(allergyIngredient),
                Set.of(100L),
                Set.of(100L)
        );

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("메뉴 후보의 재료 이름은 중복 제거")
    void should_remove_duplicate_ingredient_names() {
        Ingredient tofu1 = ingredient(100L);
        Ingredient tofu2 = ingredient(101L);

        Menu menu = menu(1L, "두부조림", DishType.SIDE_DISH);
        MenuIngredient first = menuIngredient(menu, tofu1);
        MenuIngredient second = menuIngredient(menu, tofu2);

        when(menuRepository.findAll()).thenReturn(List.of(menu));

        when(menuIngredientRepository.findAllByMenuInFetchIngredient(anyList()))
                .thenReturn(List.of(first, second));

        List<AiDietGenerateDto.MenuCandidate> result =
                menuCandidateSelector.select(
                        1,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 1),
                        List.of(),
                        Set.of(),
                        Set.of()
                );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().ingredients()).containsExactly("두부");
    }


    @Test
    @DisplayName("조회할 메뉴가 없으면 메뉴 재료 Map은 비어 있음")
    void should_return_empty_map_when_menu_is_empty() {
        Map<Long, List<MenuIngredient>> result =
                menuCandidateSelector.getMenuIngredientMap(
                        List.of()
                );

        assertThat(result).isEmpty();
        verify(menuIngredientRepository, never()).findAllByMenuInFetchIngredient(anyList());
    }


    @Test
    @DisplayName("메뉴별로 재료를 그룹화")
    void should_group_menu_ingredients_by_menu_id() {
        Menu menu1 = mock(Menu.class);
        Menu menu2 = mock(Menu.class);

        MenuIngredient first = mock(MenuIngredient.class);
        MenuIngredient second = mock(MenuIngredient.class);
        MenuIngredient third = mock(MenuIngredient.class);

        when(menu1.getId()).thenReturn(1L);
        when(menu2.getId()).thenReturn(2L);

        when(first.getMenu()).thenReturn(menu1);
        when(second.getMenu()).thenReturn(menu1);
        when(third.getMenu()).thenReturn(menu2);

        when(menuIngredientRepository.findAllByMenuInFetchIngredient(List.of(menu1, menu2)))
                .thenReturn(List.of(
                        first,
                        second,
                        third
                ));

        Map<Long, List<MenuIngredient>> result = menuCandidateSelector.getMenuIngredientMap(
                List.of(menu1, menu2));

        assertThat(result.get(1L)).containsExactly(first, second);
        assertThat(result.get(2L)).containsExactly(third);
    }


    private List<Menu> createMenus(
            long startId,
            int count,
            DishType dishType
    ) {
        List<Menu> menus = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            long id = startId + i;

            menus.add(menu(id, "메뉴" + id, dishType));
        }

        return menus;
    }


    private Menu menu(
            Long id,
            String name,
            DishType dishType
    ) {
        Menu menu = mock(Menu.class);

        when(menu.getId()).thenReturn(id);
        when(menu.getMenuName()).thenReturn(name);
        when(menu.getDishType()).thenReturn(dishType);

        return menu;
    }


    private Ingredient ingredient(
            Long id
    ) {
        Ingredient ingredient = mock(Ingredient.class);

        when(ingredient.getId()).thenReturn(id);
        when(ingredient.getName()).thenReturn("두부");

        return ingredient;
    }


    private MenuIngredient menuIngredient(
            Menu menu,
            Ingredient ingredient
    ) {
        MenuIngredient menuIngredient = mock(MenuIngredient.class);

        when(menuIngredient.getMenu()).thenReturn(menu);
        when(menuIngredient.getIngredient()).thenReturn(ingredient);

        return menuIngredient;
    }
}
