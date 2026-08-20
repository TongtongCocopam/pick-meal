package kongju.pickmeal.application.diet.generation;

import java.util.List;
import java.util.Iterator;
import java.time.LocalDate;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;


@Slf4j
@Component
public class AiDietMealPlanAssembler {

    /**
     * 식단 날짜에 넣기
     *
     * @param result  ai 식단 재배치
     * @param command 전처리 데이터
     * @return 식단 계획표
     */
    List<AiDietGenerateDto.MealPlan> create(AiDietGenerateDto.Result result, AiDietGenerateDto.Command command) {
        List<AiDietGenerateDto.MealPlan> mealPlans = new ArrayList<>();
        Iterator<Long> soupIterator = result.soupMenuIds().iterator();
        Iterator<Long> sideDishIterator = result.sideDishMenuIds().iterator();
        List<MealType> mealTypes = determineMealTypes(command.dailyMealCount());

        for (LocalDate date = command.startDate(); !date.isAfter(command.endDate()); date = date.plusDays(1)) {
            for (MealType mealType : mealTypes) {
                Long soupMenuId = getNextMenuId(soupIterator, DishType.SOUP, date, mealType);
                Long firstSideDishMenuId = getNextMenuId(sideDishIterator, DishType.SIDE_DISH, date, mealType);
                Long secondSideDishMenuId = getNextMenuId(sideDishIterator, DishType.SIDE_DISH, date, mealType);

                mealPlans.add(AiDietGenerateDto.MealPlan.builder()
                        .date(date).mealType(mealType).menuId(soupMenuId)
                        .build());

                mealPlans.add(AiDietGenerateDto.MealPlan.builder()
                        .date(date).mealType(mealType).menuId(firstSideDishMenuId)
                        .build());

                mealPlans.add(AiDietGenerateDto.MealPlan.builder()
                        .date(date).mealType(mealType).menuId(secondSideDishMenuId)
                        .build());
            }
        }
        return mealPlans;
    }

    /**
     * 식단 타입
     *
     * @param dailyMealCount 하루 끼니
     * @return 아,점,저 선택
     */
    private List<MealType> determineMealTypes(int dailyMealCount) {
        return switch (dailyMealCount) {
            case 1 -> List.of(MealType.DINNER);
            case 2 -> List.of(MealType.LUNCH, MealType.DINNER);
            case 3 -> List.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);

            default -> {
                log.error("지원하지 않는 하루 식사 횟수: dailyMealCount={}", dailyMealCount);

                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }
        };
    }

    /**
     * 메뉴 아이디 꺼내기
     *
     * @param iterator 메뉴 배열
     * @param dishType 메인, 사이드, 국 타입
     * @param date     날짜
     * @param mealType 아,점,저
     * @return 다음 메뉴 반환
     */
    private Long getNextMenuId(Iterator<Long> iterator, DishType dishType, LocalDate date, MealType mealType) {
        if (!iterator.hasNext()) {
            log.error("AI 추천 메뉴 수 부족: dishType={}, date={}, mealType={}", dishType, date, mealType);
            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        return iterator.next();
    }
}
