package kongju.pickmeal.application.diet.generation;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;


@Slf4j
@Component
public class AiDietResultRepairer {
    private static final int MAX_REPAIR_DUPLICATE_COUNT = 3;

    /**
     * 중복 메뉴 교체
     * @param result ai 식단
     * @param command 전처리 데이터
     * @return 중복 제거
     */
    public AiDietGenerateDto.Result repair(
            AiDietGenerateDto.Result result,
            AiDietGenerateDto.Command command
    ) {
        // null이면 repair 대상이 아니라 잘못된 응답
        if (result == null
                || result.soupMenuIds() == null
                || result.sideDishMenuIds() == null) {
            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        List<Long> soupCandidateIds = getCandidateIds(
                command,
                DishType.SOUP
        );

        List<Long> sideDishCandidateIds = getCandidateIds(
                command,
                DishType.SIDE_DISH
        );

        List<Long> repairedSoupIds = repairDuplicateIds(
                result.soupMenuIds(),
                soupCandidateIds,
                DishType.SOUP
        );

        List<Long> repairedSideDishIds = repairDuplicateIds(
                result.sideDishMenuIds(),
                sideDishCandidateIds,
                DishType.SIDE_DISH
        );

        return AiDietGenerateDto.Result.builder()
                .soupMenuIds(repairedSoupIds)
                .sideDishMenuIds(repairedSideDishIds)
                .build();
    }

    /**
     * 중복 아이디 찾아 교체
     * @param resultIds 결과 메뉴 리스트
     * @param candidateIds 후보 리스트
     * @param dishType 디쉬 타입
     * @return 교체된 결과 메뉴 리스트
     */
    private List<Long> repairDuplicateIds(
            List<Long> resultIds,
            List<Long> candidateIds,
            DishType dishType
    ) {
        Set<Long> used = new HashSet<>();
        List<Integer> duplicateIndexes = new ArrayList<>();

        for (int i = 0; i < resultIds.size(); i++) {
            Long menuId = resultIds.get(i);

            // null은 자동 보정하지 않음
            if (menuId == null) {
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            if (!used.add(menuId)) {
                duplicateIndexes.add(i);
            }
        }

        if (duplicateIndexes.isEmpty()) {
            return resultIds;
        }

        if (duplicateIndexes.size() > MAX_REPAIR_DUPLICATE_COUNT) {
            log.error(
                    "AI 추천 메뉴 중복 허용 범위 초과: dishType={}, duplicateCount={}",
                    dishType,
                    duplicateIndexes.size()
            );

            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        Queue<Long> missingIds = candidateIds.stream()
                .filter(id -> !used.contains(id))
                .collect(Collectors.toCollection(ArrayDeque::new));

        if (missingIds.size() < duplicateIndexes.size()) {
            log.error(
                    "AI 추천 중복 메뉴 보정 불가: dishType={}, duplicateCount={}, missingCount={}",
                    dishType,
                    duplicateIndexes.size(),
                    missingIds.size()
            );

            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        List<Long> repaired = new ArrayList<>(resultIds);

        for (Integer duplicateIndex : duplicateIndexes) {
            Long replacementId = missingIds.remove();

            log.warn(
                    "AI 추천 중복 메뉴 보정: dishType={}, index={}, duplicateId={}, replacementId={}",
                    dishType,
                    duplicateIndex,
                    repaired.get(duplicateIndex),
                    replacementId
            );

            repaired.set(duplicateIndex, replacementId);
        }

        return repaired;
    }

    /**
     * 후보 메뉴 합침
     * @param command 전처리 데이터
     * @param dishType 디쉬 타입
     * @return 후보군
     */
    private List<Long> getCandidateIds(
            AiDietGenerateDto.Command command,
            DishType dishType
    ) {
        Set<Long> seen = new HashSet<>();

        return Stream.concat(
                        command.userMenus().stream()
                                .filter(menu -> menu.dishType() == dishType)
                                .map(AiDietGenerateDto.UserMenu::menuId),

                        command.menuCandidates().stream()
                                .filter(menu -> menu.dishType() == dishType)
                                .map(AiDietGenerateDto.MenuCandidate::menuId)
                )
                .filter(seen::add)
                .toList();
    }

}
