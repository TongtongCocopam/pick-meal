package kongju.pickmeal.application.diet;

import java.util.List;
import java.util.Optional;
import java.time.YearMonth;
import java.time.LocalDate;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.support.fixture.DietFixture;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.user.UserPickCount;
import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.support.fixture.UserFixture;
import kongju.pickmeal.support.fixture.MenuFixture;
import kongju.pickmeal.core.diet.type.DietMenuSource;
import kongju.pickmeal.core.menu.type.IngredientType;
import kongju.pickmeal.support.fixture.FamilyFixture;
import kongju.pickmeal.core.menu.type.IngredientUnit;
import kongju.pickmeal.application.diet.data.DietDto;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.application.diet.data.DietMenuDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.DietRepository;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.diet.repository.UserMenuPickRepository;
import kongju.pickmeal.core.user.repository.UserPickCountRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;
import kongju.pickmeal.core.user.repository.PickCountHistoryRepository;


@ExtendWith(SpringExtension.class)
public class DietServiceTest {
    @Mock
    private UserReader userReader;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private UserMenuPickRepository userMenuPickRepository;
    @Mock
    private UserPickCountRepository userPickCountRepository;
    @Mock
    private PickCountHistoryRepository pickCountHistoryRepository;
    @Mock
    private MenuIngredientRepository menuIngredientRepository;
    @Mock
    private DietRepository dietRepository;
    @InjectMocks
    private DietService dietService;

    @Nested
    @DisplayName("선택권 사용")
    class UsePickCount {
        @Test
        @DisplayName("메뉴가 없을 경우")
        public void should_fail_menu_pick_when_menu_not_found() {
            Long userId = 1L;

            User user = UserFixture.user();
            given(userReader.getById(userId)).willReturn(user);

            given(menuRepository.findById(any())).willReturn(Optional.empty());

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .targetMonth(YearMonth.now())
                    .build();

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> dietService.menuPick(userId, request));

            assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("탈퇴한 가족의 id일 경우")
        public void should_fail_menu_pick_when_not_my_family() {
            Long userId = 1L;

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .targetMonth(YearMonth.now())
                    .build();

            given(userReader.getById(any())).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> dietService.menuPick(userId, request));

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("선택권 개수 초과")
        public void should_fail_menu_pick_when_pick_count_exceed() {
            Long userId = 1L;

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .targetMonth(YearMonth.now())
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Menu menu = MenuFixture.menu();
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            UserPickCount userPickCount = UserPickCount.initialize(user);
            given(userPickCountRepository.findByUser(any())).willReturn(Optional.of(userPickCount));

            given(pickCountHistoryRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> dietService.menuPick(userId, request));

            assertEquals(ErrorCode.TOO_MANY_SELECTIONS, exception.getErrorCode());

        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_menu_pick() {
            Long userId = 1L;
            LocalDate targetMonth = LocalDate.now();

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .targetMonth(YearMonth.now())
                    .build();
            // 선택권 사용 유저
            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            // 선택한 메뉴
            Menu menu1 = MenuFixture.menu();
            Menu menu2 = MenuFixture.menu();

            given(menuRepository.findById(1L)).willReturn(Optional.of(menu1));
            given(menuRepository.findById(2L)).willReturn(Optional.of(menu2));

            // 개수 부여
            UserPickCount userPickCount = UserPickCount.initialize(user);
            userPickCount.restoreCount(5L);

            // 개수 차감할 객체
            given(userPickCountRepository.findByUser(user)).willReturn(Optional.of(userPickCount));
            // 히스토리 저장
            given(pickCountHistoryRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            given(userMenuPickRepository.saveAll(anyList()))
                    .willReturn(
                            List.of(UserMenuPick.create(user, menu1, targetMonth),
                                    UserMenuPick.create(user, menu2, targetMonth)));

            MenuPickDto.CreateResponse response = dietService.menuPick(userId, request);

            assertEquals(2, response.pickedCount());
        }
    }

    @Nested
    @DisplayName("선택한 메뉴 변경")
    class UpdatePickMenu {
        @Test
        @DisplayName("유저 찾기 실패")
        public void should_fail_update_pick_menu_when_user_not_found() {
            Long userId = 1L;
            Long pickId = 2L;
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(1L)
                    .build();

            given(userReader.getById(any())).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("이전 선택했던 메뉴 정보 없음")
        public void should_fail_update_pick_menu_when_prev_choice_not_found() {
            Long userId = 1L;
            Long pickId = 1L;
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(1L)
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("메뉴 찾기 실패")
        public void should_fail_update_pick_menu_when_menu_not_found() {
            Long userId = 1L;
            Long pickId = 1L;
            LocalDate targetMonth = LocalDate.now();
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(2L)
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Menu menu = MenuFixture.menu();
            UserMenuPick userMenuPick = UserMenuPick.create(user, menu, targetMonth);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.of(userMenuPick));

            given(menuRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("이전과 동일한 메뉴")
        public void should_fail_update_pick_menu_when_not_change_menu() {
            Long userId = 1L;
            Long pickId = 1L;
            LocalDate targetMonth = LocalDate.now();
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(1L)
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Menu menu = MenuFixture.menu();
            given(menuRepository.findById(pickId)).willReturn(Optional.of(menu));
            UserMenuPick userMenuPick = UserMenuPick.create(user, menu, targetMonth);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.of(userMenuPick));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.MENU_PICK_NOT_CHANGED, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_update_pick_menu() {
            Long userId = 1L;
            Long pickId = 1L;
            LocalDate targetMonth = LocalDate.now();
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(2L)
                    .build();

            // 유저
            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            // 교체할 메뉴
            Menu menu = MenuFixture.menu();
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            Menu menu1 = MenuFixture.menu("마라탕");
            UserMenuPick userMenuPick = UserMenuPick.create(user, menu1, targetMonth);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.of(userMenuPick));

            MenuPickDto.UpdateResponse response = dietService.updatePickMenu(userId, pickId, request);

            assertEquals(menu.getMenuName(), response.menuName());
        }
    }

    @Nested
    @DisplayName("메뉴 선택 삭제")
    class DeleteMenuPick {
        @Test
        @DisplayName("유저 찾기 실패")
        public void should_fail_delete_pick_menu_when_user_not_found() {
            Long userId = 1L;
            Long pickId = 2L;

            given(userReader.getById(any())).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.deletePickMenu(userId, pickId));

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }


        @Test
        @DisplayName("이전 선택했던 메뉴 정보 없음")
        public void should_fail_delete_pick_menu_when_prev_choice_not_found() {
            Long userId = 1L;
            Long pickId = 1L;

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.deletePickMenu(userId, pickId));

            assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_delete_pick_menu() {
            Long userId = 1L;
            Long pickId = 1L;
            LocalDate targetMonth = LocalDate.now();

            // 유저
            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Menu menu = MenuFixture.menu();
            UserMenuPick userMenuPick = UserMenuPick.create(user, menu, targetMonth);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.of(userMenuPick));
            MenuPickDto.DeleteResponse response = dietService.deletePickMenu(userId, pickId);

            assertEquals(menu.getId(), response.menuId());
        }
    }

    @Nested
    @DisplayName("식단 보기")
    class DietView {
        @Test
        @DisplayName("식단이 비어있는 경우")
        public void should_success_diet_view_when_empty_diets() {
            Long userId = 1L;
            YearMonth month = YearMonth.now();

            // 유저
            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findMonthlyDiets(any(), any(), any())).willReturn(List.of());

            DietDto.ListItemResponse response = dietService.getDiets(userId, month);

            assertEquals(false, response.isGenerated());
            assertEquals(month, response.month());
            assertEquals(0, response.totalDays());

        }

        @Test
        @DisplayName("식단이 있는 경우")
        public void should_success_diet_view() {
            Long userId = 1L;
            YearMonth month = YearMonth.now();

            // 유저
            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Family family = FamilyFixture.family();
            Menu menu = MenuFixture.menu();

            DietGeneration dg = DietGeneration.createPending(
                    family,
                    LocalDate.now(),
                    LocalDate.now(),
                    1,
                    LocalDate.now()
            );
            Diet diet = Diet.create(family,
                    menu,
                    LocalDate.now(),
                    MealType.BREAKFAST,
                    dg,
                    DietMenuSource.USER_PICKED);
            given(dietRepository.findMonthlyDiets(any(), any(), any())).willReturn(List.of(diet));

            DietDto.ListItemResponse response = dietService.getDiets(userId, month);

            assertEquals(true, response.isGenerated());
            assertEquals(month, response.month());
            assertEquals(1, response.totalDays());

            DietDto.MealResponse mealResponse = response.diets().getFirst().meals().getFirst();
            assertEquals(MealType.BREAKFAST, mealResponse.mealType());
        }

    }

    @Nested
    @DisplayName("일일 식단 보기")
    class DailyMeal {
        @Test
        @DisplayName("식단이 없는 경우")
        public void should_fail_daily_meal_when_diet_not_exist() {
            given(userReader.getById(any())).willReturn(UserFixture.user());
            given(dietRepository.findAllFamilyAndMealDate(any(), any())).willReturn(List.of());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.getDailyMeals(1L, LocalDate.ofEpochDay(2026 - 7 - 1)));

            assertEquals(ErrorCode.DIET_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_daily_meal() {
            Long userId = 1L;
            LocalDate date = LocalDate.now();

            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyMember(family);

            given(userReader.getById(any())).willReturn(user);

            Menu menu1 = MenuFixture.menu();
            Menu menu2 = MenuFixture.menu("계란말이");

            DietGeneration dietGeneration = DietGeneration.createPending(family, date, date, 1, LocalDate.now());

            Diet breakfastSoup = Diet.create(family, menu1, date, MealType.BREAKFAST, dietGeneration, DietMenuSource.USER_PICKED);
            Diet breakfastSide = Diet.create(family, menu2, date, MealType.BREAKFAST, dietGeneration, DietMenuSource.USER_PICKED);
            given(dietRepository.findAllFamilyAndMealDate(any(), any())).willReturn(List.of(breakfastSoup, breakfastSide));

            Ingredient kimchi = Ingredient.create("김치");
            Ingredient egg = Ingredient.create("계란");
            given(menuIngredientRepository.findAllByMenuWithIngredient(menu1))
                    .willReturn(List.of(MenuIngredient.create(menu1, kimchi, "100.0", 100.0, IngredientUnit.G, IngredientType.MAIN)));

            given(menuIngredientRepository.findAllByMenuWithIngredient(menu2))
                    .willReturn(List.of(MenuIngredient.create(menu2, egg, "2.0", 2.0, IngredientUnit.PIECE, IngredientType.SUB)));

            DietDto.DailyDetailResponse response =
                    dietService.getDailyMeals(userId, date);

            assertThat(response.date()).isEqualTo(date);
            System.out.println(response);
        }
    }

    @Nested
    @DisplayName("ai생성 식단 메뉴 대체")
    class ReplaceMenu {
        @Test
        @DisplayName("식단이 없음")
        public void should_fail_replace_meal_when_diet_not_exist() {
            Long userId = 1L;
            Long dietId = 2L;
            DietMenuDto.ReplaceRequest request = DietMenuDto.ReplaceRequest.builder()
                    .menuId(3L)
                    .build();
            given(userReader.getById(any())).willReturn(UserFixture.user());
            given(dietRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.replaceMenu(userId, dietId, request));

            assertEquals(ErrorCode.DIET_ITEM_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("대체할 메뉴가 없음")
        public void should_success_replace_meal_when_not_exist_menu() {
            Long userId = 1L;
            Long dietId = 2L;
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);
            Menu menu = MenuFixture.menu();
            DietGeneration dietGeneration = DietGeneration.createPending(family, LocalDate.now(), LocalDate.now(), 1, LocalDate.now());
            Diet diet = Diet.create(family, menu, LocalDate.now(), MealType.BREAKFAST, dietGeneration, DietMenuSource.USER_PICKED);

            DietMenuDto.ReplaceRequest request = DietMenuDto.ReplaceRequest.builder()
                    .menuId(3L)
                    .build();

            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findById(any())).willReturn(Optional.of(diet));
            given(menuRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.replaceMenu(userId, dietId, request));

            assertEquals(ErrorCode.ITEM_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_replace_meal() {
            Long userId = 1L;
            Long dietId = 2L;
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);
            Menu menu = MenuFixture.menu();
            DietGeneration dietGeneration = DietGeneration.createPending(family, LocalDate.now(), LocalDate.now(), 1, LocalDate.now());
            Diet diet = Diet.create(family, menu, LocalDate.now(), MealType.BREAKFAST, dietGeneration, DietMenuSource.USER_PICKED);
            Menu menu2 = MenuFixture.menu("계란말이");

            DietMenuDto.ReplaceRequest request = DietMenuDto.ReplaceRequest.builder()
                    .menuId(3L)
                    .build();

            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findById(any())).willReturn(Optional.of(diet));
            given(menuRepository.findById(any())).willReturn(Optional.of(menu2));

            DietMenuDto.ReplaceResponse response = dietService.replaceMenu(userId, dietId, request);

            assertEquals(menu2.getId(), response.replacedMenuId());
            assertEquals(menu2.getMenuName(), response.menuName());
        }
    }

    @Nested
    @DisplayName("생성 식단 대체 가능 메뉴 목록")
    class ReplaceMenus {
        @Test
        @DisplayName("유저가 선택함 식단이면 교체 불가")
        public void should_fail_replace_menus_when_diet_not_found() {
            Long userId = 1L;
            Long dietId = 2L;
            String keyword = "김치";
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);


            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.replacementMenus(userId, dietId, keyword, null));

            assertEquals(ErrorCode.DIET_ITEM_NOT_FOUND, exception.getErrorCode());

        }

        @Test
        @DisplayName("내 가족 식단인지 확인")
        public void should_fail_replace_menus_when_not_my_family() {
            Long userId = 1L;
            Long dietId = 2L;
            String keyword = "김치";
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            Family family2 = FamilyFixture.family();
            user.joinFamilyLeader(family);
            Menu menu = MenuFixture.menu();
            DietGeneration dietGeneration = DietGeneration.createPending(family, LocalDate.now(), LocalDate.now(), 1, LocalDate.now());
            Diet diet = DietFixture.diet(family2, menu, dietGeneration, DietMenuSource.AI_RECOMMENDED);

            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findById(any())).willReturn(Optional.of(diet));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.replacementMenus(userId, dietId, keyword, null));

            assertEquals(ErrorCode.NOT_FAMILY_MEMBER, exception.getErrorCode());

        }

        @Test
        @DisplayName("유저가 선택함 식단이면 교체 불가")
        public void should_fail_replace_menus_when_user_choice_menu() {
            Long userId = 1L;
            Long dietId = 2L;
            String keyword = "김치";
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);
            Menu menu = MenuFixture.menu();
            DietGeneration dietGeneration = DietGeneration.createPending(family, LocalDate.now(), LocalDate.now(), 1, LocalDate.now());
            Diet diet = DietFixture.diet(family, menu, dietGeneration, DietMenuSource.USER_PICKED);

            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findById(any())).willReturn(Optional.of(diet));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.replacementMenus(userId, dietId, keyword, null));

            assertEquals(ErrorCode.DIET_MENU_LOCKED, exception.getErrorCode());
        }

        @Test
        @DisplayName("메뉴 리스트가 비어있을 경우")
        public void should_success_replace_menus_when_menus_empty() {
            Long userId = 1L;
            Long dietId = 2L;
            String keyword = "김치";
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);
            Menu menu = MenuFixture.menu();
            DietGeneration dietGeneration = DietGeneration.createPending(family, LocalDate.now(), LocalDate.now(), 1, LocalDate.now());
            Diet diet = DietFixture.diet(family, menu, dietGeneration, DietMenuSource.AI_RECOMMENDED);

            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findById(any())).willReturn(Optional.of(diet));
            given(menuRepository.searchReplacementMenus(any(), any(), any(), any(), any())).willReturn(Page.empty());

            DietMenuDto.ReplacementMenuListResponse response = dietService.replacementMenus(userId, dietId, keyword, null);

            assertEquals(dietId, response.dietId());
            assertEquals(List.of(), response.menus());
            assertEquals(DishType.STEW, response.dishType());
        }

        @Test
        @DisplayName("성공케이스")
        public void should_success_replace_menus() {
            Long userId = 1L;
            Long dietId = 2L;
            String keyword = "김치";
            User user = UserFixture.user();
            Family family = FamilyFixture.family();
            user.joinFamilyLeader(family);
            Menu menu = MenuFixture.menu();
            DietGeneration dietGeneration = DietGeneration.createPending(family, LocalDate.now(), LocalDate.now(), 1, LocalDate.now());
            Diet diet = DietFixture.diet(family, menu, dietGeneration, DietMenuSource.AI_RECOMMENDED);

            given(userReader.getById(any())).willReturn(user);
            given(dietRepository.findById(any())).willReturn(Optional.of(diet));

            Menu menu2 = MenuFixture.menu("김치찌개");
            Menu menu3 = MenuFixture.menu("김치국");
            Pageable pageable = PageRequest.of(0, 20);
            Page<Menu> menuPage = new PageImpl<>(List.of(menu2, menu3), pageable, 2);
            given(menuRepository.searchReplacementMenus(any(), any(), any(), any(), any())).willReturn(menuPage);

            DietMenuDto.ReplacementMenuListResponse response = dietService.replacementMenus(userId, dietId, keyword, null);

            assertEquals(dietId, response.dietId());
            assertEquals(2, response.menus().size());
            assertEquals(DishType.STEW, response.dishType());
        }
    }
}
