package kongju.pickmeal.application.diet.generation;

import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.diet.type.DietMenuSource;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.DietRepository;
import kongju.pickmeal.core.menu.repository.MenuRepository;


@Service
@RequiredArgsConstructor
public class AiDietResultSaver {
    private final UserReader userReader;
    private final MenuRepository menuRepository;
    private final DietRepository dietRepository;

    /**
     * DB에 데이터 저장
     *
     * @param generation DietGeneration
     * @param mealPlans  ai 생성 결과
     * @param command    전처리 데이터
     */
    void save(DietGeneration generation, List<AiDietGenerateDto.MealPlan> mealPlans,
                          AiDietGenerateDto.Command command) {
        User user = userReader.getById(command.userId());
        Family family = user.getFamily();

        Set<Long> userPickMenuIds = command.userMenus().stream()
                .map(AiDietGenerateDto.UserMenu::menuId)
                .collect(Collectors.toSet());

        List<Diet> diets = mealPlans.stream()
                .map(mealPlan -> {
                    Menu menu = menuRepository.findById(mealPlan.menuId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
                    DietMenuSource source = userPickMenuIds.contains(mealPlan.menuId())
                            ? DietMenuSource.USER_PICKED
                            : DietMenuSource.AI_RECOMMENDED;

                    return Diet.create(family, menu, mealPlan.date(), mealPlan.mealType(), generation, source);
                })
                .toList();

        dietRepository.saveAll(diets);
        // 사용한 식단만 제거 - 아직까진 별 쓸모 없음 한번만 생성 가능하기 때문
        markUsedUserMenuPicks(mealPlans, command);
    }

    /**
     * 식단 생성에 사용된 메뉴 사용 처리
     *
     * @param mealPlans 생성 식단
     * @param command   전처리 데이터
     */
    private void markUsedUserMenuPicks(
            List<AiDietGenerateDto.MealPlan> mealPlans,
            AiDietGenerateDto.Command command
    ) {
        Set<Long> assignedMenuIds = mealPlans.stream()
                .map(AiDietGenerateDto.MealPlan::menuId)
                .collect(Collectors.toSet());

        Set<Long> usedUserMenuPickIds = command.userMenus().stream()
                .filter(userMenu ->
                        assignedMenuIds.contains(userMenu.menuId())
                )
                .map(AiDietGenerateDto.UserMenu::userMenuPickId)
                .collect(Collectors.toSet());

        command.userMenuPicks().stream()
                .filter(userMenuPick ->
                        usedUserMenuPickIds.contains(userMenuPick.getId())
                )
                .forEach(UserMenuPick::used);
    }

}
