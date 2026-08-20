package kongju.pickmeal.application.diet.generation;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.DietRepository;
import kongju.pickmeal.core.menu.repository.MenuRepository;


@ExtendWith(MockitoExtension.class)
public class AiDietResultSaverTest {
    @Mock
    private UserReader userReader;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private DietRepository dietRepository;
    @InjectMocks
    private AiDietResultSaver saver;

    @Test
    @DisplayName("AI 식단 결과를 Diet으로 생성하여 저장")
    void should_save_diets() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 9, 1);

        User user = mock(User.class);
        Family family = mock(Family.class);
        DietGeneration generation = mock(DietGeneration.class);

        Menu soup = mock(Menu.class);
        Menu sideDish1 = mock(Menu.class);
        Menu sideDish2 = mock(Menu.class);

        when(userReader.getById(userId)).thenReturn(user);
        when(user.getFamily()).thenReturn(family);
        when(menuRepository.findById(10L)).thenReturn(Optional.of(soup));
        when(menuRepository.findById(20L)).thenReturn(Optional.of(sideDish1));
        when(menuRepository.findById(30L)).thenReturn(Optional.of(sideDish2));

        List<AiDietGenerateDto.MealPlan> mealPlans = List.of(
                mealPlan(date, 10L),
                mealPlan(date, 20L),
                mealPlan(date, 30L)
        );

        AiDietGenerateDto.Command command = command(userId, List.of(), List.of());

        saver.save(generation, mealPlans, command);

        verify(menuRepository).findById(10L);
        verify(menuRepository).findById(20L);
        verify(menuRepository).findById(30L);

        verify(dietRepository).saveAll(anyList());
    }


    @Test
    @DisplayName("식단 메뉴가 존재하지 않으면 예외가 발생")
    void should_fail_when_menu_not_found() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 9, 1);

        User user = mock(User.class);
        Family family = mock(Family.class);
        DietGeneration generation = mock(DietGeneration.class);

        when(userReader.getById(userId))
                .thenReturn(user);

        when(user.getFamily())
                .thenReturn(family);

        when(menuRepository.findById(999L))
                .thenReturn(Optional.empty());

        List<AiDietGenerateDto.MealPlan> mealPlans = List.of(
                mealPlan(date, 999L)
        );

        AiDietGenerateDto.Command command =
                command(userId, List.of(), List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> saver.save(generation, mealPlans, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.MENU_NOT_FOUND);

        verify(dietRepository, never()).saveAll(anyList());
    }


    @Test
    @DisplayName("식단에 사용된 사용자 선택 메뉴는 사용 처리")
    void should_mark_used_user_menu_pick() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 9, 1);

        User user = mock(User.class);
        Family family = mock(Family.class);
        DietGeneration generation = mock(DietGeneration.class);
        Menu menu = mock(Menu.class);

        UserMenuPick userMenuPick = mock(UserMenuPick.class);

        when(userReader.getById(userId)).thenReturn(user);

        when(user.getFamily()).thenReturn(family);

        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));

        when(userMenuPick.getId()).thenReturn(100L);

        AiDietGenerateDto.UserMenu userMenu =
                AiDietGenerateDto.UserMenu.builder()
                        .userMenuPickId(100L)
                        .menuId(10L)
                        .dishType(DishType.SIDE_DISH)
                        .build();

        List<AiDietGenerateDto.MealPlan> mealPlans = List.of(
                mealPlan(date, 10L)
        );

        AiDietGenerateDto.Command command =
                command(
                        userId,
                        List.of(userMenu),
                        List.of(userMenuPick)
                );

        saver.save(generation, mealPlans, command);
        verify(userMenuPick).used();
    }


    @Test
    @DisplayName("사용자 선택 메뉴가 식단에 사용되지 않으면 사용 처리하지 않음")
    void should_not_mark_unused_user_menu_pick() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 9, 1);

        User user = mock(User.class);
        Family family = mock(Family.class);
        DietGeneration generation = mock(DietGeneration.class);
        Menu menu = mock(Menu.class);

        UserMenuPick userMenuPick = mock(UserMenuPick.class);

        when(userReader.getById(userId)).thenReturn(user);

        when(user.getFamily()).thenReturn(family);

        when(menuRepository.findById(20L)).thenReturn(Optional.of(menu));

        when(userMenuPick.getId()).thenReturn(100L);

        AiDietGenerateDto.UserMenu userMenu =
                AiDietGenerateDto.UserMenu.builder()
                        .userMenuPickId(100L)
                        .menuId(10L)
                        .dishType(DishType.SIDE_DISH)
                        .build();

        // 실제 식단에는 menuId=20만 배치됨
        List<AiDietGenerateDto.MealPlan> mealPlans = List.of(
                mealPlan(date, 20L)
        );

        AiDietGenerateDto.Command command =
                command(
                        userId,
                        List.of(userMenu),
                        List.of(userMenuPick)
                );

        saver.save(generation, mealPlans, command);
        verify(userMenuPick, never()).used();
    }


    private AiDietGenerateDto.MealPlan mealPlan(
            LocalDate date,
            Long menuId
    ) {
        return AiDietGenerateDto.MealPlan.builder()
                .date(date)
                .mealType(MealType.DINNER)
                .menuId(menuId)
                .build();
    }


    private AiDietGenerateDto.Command command(
            Long userId,
            List<AiDietGenerateDto.UserMenu> userMenus,
            List<UserMenuPick> userMenuPicks
    ) {
        return AiDietGenerateDto.Command.builder()
                .userId(userId)
                .userMenus(userMenus)
                .userMenuPicks(userMenuPicks)
                .build();
    }

}
