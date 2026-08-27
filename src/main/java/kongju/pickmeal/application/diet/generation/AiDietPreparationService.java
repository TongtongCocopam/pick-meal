package kongju.pickmeal.application.diet.generation;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.application.diet.data.FamilyDietDataDto;
import kongju.pickmeal.core.diet.repository.UserMenuPickRepository;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiDietPreparationService {
    private final UserReader userReader;
    private final UserRepository userRepository;
    private final FamilyDietDataReader familyDietDataReader;
    private final MenuCandidateSelector menuCandidateSelector;
    private final UserMenuPickRepository userMenuPickRepository;

    /**
     * Ai넣기전 전처리
     *
     * @param userId  유저 아이디
     * @param request 신청 날짜와 끼니 개수
     * @return 질병정보, 건강정보, 메뉴, 선호 비선호 재료 등
     */
    public AiDietGenerateDto.Command prepare(
            Long userId,
            DietGenerationDto.GenerateRequest request,
            LocalDate startDate,
            LocalDate endDate,
            List<Long> userMenuPickIds) {
        User user = userReader.getById(userId);
        Family family = user.getFamily();

        log.info("1. 가족 구성원");
        List<User> users = userRepository.findAllFamily(family);
        log.info("2. 가족 건강/질병/선호 데이터");
        FamilyDietDataDto familyData = familyDietDataReader.read(users);
        log.info("3. 사용자 직접 선택 메뉴");
        UserMenuPickPreparation pickPreparation = getUserMenus(userMenuPickIds);
        log.info("4. AI 후보 메뉴");
        List<AiDietGenerateDto.MenuCandidate> menuCandidates = menuCandidateSelector.select(
                request.dailyMealCount(),
                startDate,
                endDate,
                familyData.preferredIngredients(),
                familyData.allergyIngredientIds(),
                familyData.fallbackExcludedIngredientIds()
        );

        return AiDietGenerateDto.Command.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .dailyMealCount(request.dailyMealCount())

                .menuCandidates(menuCandidates)

                .userMenus(pickPreparation.userMenus())
                .userMenuPicks(pickPreparation.userMenuPicks())

                .healthConditions(familyData.healthConditions())
                .disease(familyData.diseases())

                .preferredIngredients(familyData.preferredIngredientNames())
                .dislikedIngredients(familyData.dislikedIngredientNames())
                .allergyIngredients(familyData.allergyIngredientNames())
                .build();
    }

    /**
     * 유저가 선택한 메뉴
     *
     * @param userMenuPickIds 가족 선택 메뉴 아이디
     * @return 유저 선택 메뉴 리스트
     */
    private UserMenuPickPreparation getUserMenus(List<Long> userMenuPickIds) {
        List<UserMenuPick> userMenuPicks = userMenuPickRepository.findAllByIdInFetchMenu(userMenuPickIds);

        // 중복 제거
        List<Menu> pickedMenus = userMenuPicks.stream()
                .map(UserMenuPick::getMenu)
                .collect(Collectors.toMap(
                        Menu::getId,
                        menu -> menu,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .toList();

        // 재료와 메뉴 연결
        Map<Long, List<MenuIngredient>> menuIngredientMap = menuCandidateSelector.getMenuIngredientMap(pickedMenus);

        // ai에게 넘겨줄 객체
        List<AiDietGenerateDto.UserMenu> userMenus = userMenuPicks.stream()
                .map(userMenuPick -> {
                    Menu menu = userMenuPick.getMenu();

                    List<String> ingredients = menuIngredientMap
                            .getOrDefault(menu.getId(), List.of())
                            .stream()
                            .map(menuIngredient -> menuIngredient.getIngredient().getName())
                            .distinct()
                            .toList();

                    return AiDietGenerateDto.UserMenu.builder()
                            .userMenuPickId(userMenuPick.getId())
                            .menuId(menu.getId())
                            .menuName(menu.getMenuName())
                            .dishType(menu.getDishType())
                            .ingredients(ingredients)
                            .build();
                })
                .toList();

        return UserMenuPickPreparation.builder()
                .userMenus(userMenus)
                .userMenuPicks(userMenuPicks)
                .build();
    }

    @Builder
    private record UserMenuPickPreparation(
            List<AiDietGenerateDto.UserMenu> userMenus,
            List<UserMenuPick> userMenuPicks
    ) {
    }

}
