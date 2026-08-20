package kongju.pickmeal.application.diet.generation;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.application.diet.data.FamilyDietDataDto;
import kongju.pickmeal.core.diet.repository.UserMenuPickRepository;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class AiDietPreparationServiceTest {
    @Mock
    private UserReader userReader;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FamilyDietDataReader familyDietDataReader;

    @Mock
    private MenuCandidateSelector menuCandidateSelector;

    @Mock
    private UserMenuPickRepository userMenuPickRepository;

    @InjectMocks
    private AiDietPreparationService preparationService;


    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        userId = 1L;
        startDate = LocalDate.of(2026, 9, 1);
        endDate = LocalDate.of(2026, 9, 30);
    }

    @Test
    @DisplayName("AI 식단 생성에 필요한 데이터를 조회하여 Command를 생성")
    void should_prepare_ai_diet_command() {
        User user = mock(User.class);
        Family family = mock(Family.class);
        User familyMember = mock(User.class);
        DietGenerationDto.GenerateRequest request = mock(DietGenerationDto.GenerateRequest.class);
        FamilyDietDataDto familyData = mock(FamilyDietDataDto.class);
        List<Long> userMenuPickIds = List.of();
        AiDietGenerateDto.MenuCandidate candidate = mock(AiDietGenerateDto.MenuCandidate.class);

        when(userReader.getById(userId)).thenReturn(user);
        when(user.getFamily()).thenReturn(family);
        when(userRepository.findAllFamily(family)).thenReturn(List.of(user, familyMember));
        when(request.dailyMealCount()).thenReturn(3);
        when(familyData.preferredIngredients()).thenReturn(List.of());
        when(familyData.allergyIngredientIds()).thenReturn(Set.of());
        when(familyData.fallbackExcludedIngredientIds()).thenReturn(Set.of());
        when(familyData.healthConditions()).thenReturn(List.of());
        when(familyData.diseases()).thenReturn(List.of());
        when(familyData.preferredIngredientNames()).thenReturn(List.of("두부"));
        when(familyData.dislikedIngredientNames()).thenReturn(List.of("가지"));
        when(familyData.allergyIngredientNames()).thenReturn(List.of("땅콩"));
        when(familyDietDataReader.read(List.of(user, familyMember))).thenReturn(familyData);
        when(userMenuPickRepository.findAllByIdInFetchMenu(userMenuPickIds)).thenReturn(List.of());
        when(menuCandidateSelector.getMenuIngredientMap(List.of())).thenReturn(Map.of());
        when(menuCandidateSelector.select(
                3,
                startDate,
                endDate,
                familyData.preferredIngredients(),
                familyData.allergyIngredientIds(),
                familyData.fallbackExcludedIngredientIds()
        )).thenReturn(List.of(candidate));

        AiDietGenerateDto.Command command =
                preparationService.prepare(
                        userId,
                        request,
                        startDate,
                        endDate,
                        userMenuPickIds
                );

        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.startDate()).isEqualTo(startDate);
        assertThat(command.endDate()).isEqualTo(endDate);
        assertThat(command.dailyMealCount()).isEqualTo(3);

        assertThat(command.menuCandidates()).containsExactly(candidate);

        assertThat(command.userMenus()).isEmpty();

        assertThat(command.userMenuPicks()).isEmpty();

        assertThat(command.preferredIngredients()).containsExactly("두부");

        assertThat(command.dislikedIngredients()).containsExactly("가지");

        assertThat(command.allergyIngredients()).containsExactly("땅콩");
    }

    @Test
    @DisplayName("사용자 선택 메뉴를 AI용 UserMenu로 변환")
    void should_convert_user_menu_pick_to_ai_user_menu() {
        User user = mock(User.class);
        Family family = mock(Family.class);
        DietGenerationDto.GenerateRequest request = mock(DietGenerationDto.GenerateRequest.class);
        FamilyDietDataDto familyData = mock(FamilyDietDataDto.class);
        UserMenuPick userMenuPick = mock(UserMenuPick.class);
        Menu menu = mock(Menu.class);

        var firstIngredient = mock(
                kongju.pickmeal.core.menu.MenuIngredient.class,
                RETURNS_DEEP_STUBS
        );

        var secondIngredient = mock(
                kongju.pickmeal.core.menu.MenuIngredient.class,
                RETURNS_DEEP_STUBS
        );
        when(userReader.getById(userId)).thenReturn(user);
        when(user.getFamily()).thenReturn(family);
        when(userRepository.findAllFamily(family)).thenReturn(List.of(user));
        when(request.dailyMealCount()).thenReturn(1);
        when(familyDietDataReader.read(List.of(user))).thenReturn(familyData);

        when(familyData.preferredIngredients()).thenReturn(List.of());
        when(familyData.allergyIngredientIds()).thenReturn(Set.of());
        when(familyData.fallbackExcludedIngredientIds()).thenReturn(Set.of());
        when(familyData.healthConditions()).thenReturn(List.of());
        when(familyData.diseases()).thenReturn(List.of());
        when(familyData.preferredIngredientNames()).thenReturn(List.of());
        when(familyData.dislikedIngredientNames()).thenReturn(List.of());
        when(familyData.allergyIngredientNames()).thenReturn(List.of());

        when(userMenuPickRepository.findAllByIdInFetchMenu(List.of(100L))).thenReturn(List.of(userMenuPick));

        when(userMenuPick.getId()).thenReturn(100L);
        when(userMenuPick.getMenu()).thenReturn(menu);
        when(menu.getId()).thenReturn(10L);
        when(menu.getMenuName()).thenReturn("두부조림");
        when(menu.getDishType()).thenReturn(DishType.SIDE_DISH);
        when(firstIngredient.getIngredient().getName()).thenReturn("두부");
        when(secondIngredient.getIngredient().getName()).thenReturn("간장");

        when(menuCandidateSelector.getMenuIngredientMap(List.of(menu)))
                .thenReturn(
                        Map.of(
                                10L,
                                List.of(firstIngredient, secondIngredient)
                        )
                );

        when(menuCandidateSelector.select(
                anyInt(),
                any(),
                any(),
                anyList(),
                anySet(),
                anySet()
        )).thenReturn(List.of());

        AiDietGenerateDto.Command command =
                preparationService.prepare(
                        userId,
                        request,
                        startDate,
                        endDate,
                        List.of(100L)
                );

        assertThat(command.userMenus()).hasSize(1);
        AiDietGenerateDto.UserMenu userMenu = command.userMenus().getFirst();
        assertThat(userMenu.userMenuPickId()).isEqualTo(100L);
        assertThat(userMenu.menuId()).isEqualTo(10L);
        assertThat(userMenu.menuName()).isEqualTo("두부조림");
        assertThat(userMenu.dishType()).isEqualTo(DishType.SIDE_DISH);
        assertThat(userMenu.ingredients()).containsExactly("두부", "간장");
        assertThat(command.userMenuPicks()).containsExactly(userMenuPick);
    }
}
