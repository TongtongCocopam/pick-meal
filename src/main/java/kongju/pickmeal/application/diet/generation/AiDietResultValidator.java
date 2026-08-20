package kongju.pickmeal.application.diet.generation;

import java.util.*;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;


@Slf4j
@Component
public class AiDietResultValidator {
    /**
     * ai생성 데이터 검증
     *
     * @param result  생성 데이터
     * @param command 전처리 데이터
     */
    public void validate(AiDietGenerateDto.Result result, AiDietGenerateDto.Command command) {
        validateRequiredCount(result, command);
        Map<Long, DishType> dishTypeMap = buildDishTypeMap(command);

        validateRankedIds(
                result.soupMenuIds(),
                DishType.SOUP,
                dishTypeMap
        );

        validateRankedIds(
                result.sideDishMenuIds(),
                DishType.SIDE_DISH,
                dishTypeMap
        );
    }

    private void validateRankedIds(
            List<Long> menuIds,
            DishType expectedDishType,
            Map<Long, DishType> dishTypeMap
    ) {
        if (menuIds == null || menuIds.isEmpty()) {
            log.error("AI 추천 메뉴 목록이 비어 있음: dishType={}", expectedDishType);
            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        Set<Long> uniqueMenuIds = new HashSet<>();

        for (Long menuId : menuIds) {
            if (menuId == null) {
                log.error("AI 추천 menuId가 null임: dishType={}", expectedDishType);
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            if (!uniqueMenuIds.add(menuId)) {
                log.error("AI 추천 menuId 중복: dishType={}, menuId={}", expectedDishType, menuId
                );
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            DishType actualDishType = dishTypeMap.get(menuId);

            if (actualDishType == null) {
                log.error("AI가 후보에 없는 menuId를 반환함: expectedDishType={}, menuId={}", expectedDishType, menuId);
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            if (actualDishType != expectedDishType) {
                log.error("AI 추천 메뉴 타입 불일치: menuId={}, expected={}, actual={}", menuId, expectedDishType, actualDishType
                );
                throw new BusinessException(ErrorCode.INVALID_MENU_DATA);
            }
        }
    }

    /**
     * 메뉴 후보에서 id, dishType추출
     *
     * @param command ai에 넣을 전처리한 데이터
     * @return map
     */
    private Map<Long, DishType> buildDishTypeMap(
            AiDietGenerateDto.Command command
    ) {
        Map<Long, DishType> dishTypeMap = new HashMap<>();

        for (AiDietGenerateDto.MenuCandidate candidate : command.menuCandidates()) {
            dishTypeMap.put(candidate.menuId(), candidate.dishType());
        }

        for (AiDietGenerateDto.UserMenu userMenu : command.userMenus()) {
            dishTypeMap.putIfAbsent(userMenu.menuId(), userMenu.dishType());
        }

        return dishTypeMap;
    }

    /**
     * 데이터 검증
     *
     * @param result  결과
     * @param command 전처리 데이터
     */
    private void validateRequiredCount(
            AiDietGenerateDto.Result result,
            AiDietGenerateDto.Command command
    ) {
        int dayCount = Math.toIntExact(ChronoUnit.DAYS.between(command.startDate(), command.endDate()) + 1);

        int requiredSoupCount = dayCount * command.dailyMealCount();

        int requiredSideDishCount = requiredSoupCount * 2;

        if (result.soupMenuIds().size() != requiredSoupCount) {
            log.error(
                    "AI 국 메뉴 개수 불일치: required={}, actual={}",
                    requiredSoupCount,
                    result.soupMenuIds().size()
            );

            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        if (result.sideDishMenuIds().size() != requiredSideDishCount) {
            log.error(
                    "AI 반찬 메뉴 개수 불일치: required={}, actual={}",
                    requiredSideDishCount,
                    result.sideDishMenuIds().size()
            );

            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }
    }
}
