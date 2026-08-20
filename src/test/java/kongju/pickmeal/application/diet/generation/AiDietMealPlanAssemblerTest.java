package kongju.pickmeal.application.diet.generation;

import java.util.List;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;

import static org.assertj.core.api.Assertions.*;


public class AiDietMealPlanAssemblerTest {
    private final AiDietMealPlanAssembler assembler =
            new AiDietMealPlanAssembler();

    @Test
    @DisplayName("하루 1끼이면 저녁에 국 1개와 반찬 2개를 배치")
    void should_create_dinner_when_daily_meal_count_is_one() {
        LocalDate date = LocalDate.of(2026, 9, 1);

        AiDietGenerateDto.Command command = command(
                date,
                date,
                1
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L),
                List.of(2L, 3L)
        );

        List<AiDietGenerateDto.MealPlan> mealPlans = assembler.create(result, command);

        assertThat(mealPlans).hasSize(3);

        assertThat(mealPlans)
                .extracting(
                        AiDietGenerateDto.MealPlan::date,
                        AiDietGenerateDto.MealPlan::mealType,
                        AiDietGenerateDto.MealPlan::menuId
                )
                .containsExactly(
                        tuple(date, MealType.DINNER, 1L),
                        tuple(date, MealType.DINNER, 2L),
                        tuple(date, MealType.DINNER, 3L)
                );
    }


    @Test
    @DisplayName("하루 2끼이면 점심과 저녁 순서로 식단을 배치")
    void should_create_lunch_and_dinner_when_daily_meal_count_is_two() {
        LocalDate date = LocalDate.of(2026, 9, 1);

        AiDietGenerateDto.Command command = command(
                date,
                date,
                2
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 2L),
                List.of(11L, 12L, 13L, 14L)
        );

        List<AiDietGenerateDto.MealPlan> mealPlans = assembler.create(result, command);

        assertThat(mealPlans).hasSize(6);

        assertThat(mealPlans)
                .extracting(
                        AiDietGenerateDto.MealPlan::mealType,
                        AiDietGenerateDto.MealPlan::menuId
                )
                .containsExactly(
                        tuple(MealType.LUNCH, 1L),
                        tuple(MealType.LUNCH, 11L),
                        tuple(MealType.LUNCH, 12L),

                        tuple(MealType.DINNER, 2L),
                        tuple(MealType.DINNER, 13L),
                        tuple(MealType.DINNER, 14L)
                );
    }


    @Test
    @DisplayName("하루 3끼이면 아침 점심 저녁 순서로 식단을 배치")
    void should_create_breakfast_lunch_and_dinner_when_daily_meal_count_is_three() {
        LocalDate date = LocalDate.of(2026, 9, 1);

        AiDietGenerateDto.Command command = command(
                date,
                date,
                3
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 2L, 3L),
                List.of(11L, 12L, 13L, 14L, 15L, 16L)
        );

        List<AiDietGenerateDto.MealPlan> mealPlans = assembler.create(result, command);

        assertThat(mealPlans).hasSize(9);

        assertThat(mealPlans)
                .extracting(
                        AiDietGenerateDto.MealPlan::mealType
                )
                .containsExactly(
                        MealType.BREAKFAST,
                        MealType.BREAKFAST,
                        MealType.BREAKFAST,

                        MealType.LUNCH,
                        MealType.LUNCH,
                        MealType.LUNCH,

                        MealType.DINNER,
                        MealType.DINNER,
                        MealType.DINNER
                );

        assertThat(mealPlans)
                .extracting(
                        AiDietGenerateDto.MealPlan::menuId
                )
                .containsExactly(
                        1L, 11L, 12L,
                        2L, 13L, 14L,
                        3L, 15L, 16L
                );
    }


    @Test
    @DisplayName("여러 날짜이면 시작일부터 종료일까지 순서대로 식단을 배치")
    void should_create_meal_plans_in_date_order() {
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 2);

        AiDietGenerateDto.Command command = command(
                startDate,
                endDate,
                1
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 2L),
                List.of(11L, 12L, 13L, 14L)
        );

        List<AiDietGenerateDto.MealPlan> mealPlans = assembler.create(result, command);

        assertThat(mealPlans).hasSize(6);

        assertThat(mealPlans)
                .extracting(
                        AiDietGenerateDto.MealPlan::date
                )
                .containsExactly(
                        startDate,
                        startDate,
                        startDate,
                        endDate,
                        endDate,
                        endDate
                );

        assertThat(mealPlans)
                .extracting(
                        AiDietGenerateDto.MealPlan::menuId
                )
                .containsExactly(
                        1L, 11L, 12L,
                        2L, 13L, 14L
                );
    }


    @Test
    @DisplayName("하루 식사 횟수가 1~3이 아니면 실패한다")
    void should_fail_when_daily_meal_count_is_invalid() {
        LocalDate date = LocalDate.of(2026, 9, 1);

        AiDietGenerateDto.Command command = command(
                date,
                date,
                4
        );

        AiDietGenerateDto.Result result = result(
                List.of(),
                List.of()
        );

        assertThatThrownBy(
                () -> assembler.create(result, command)
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                        assertThat(
                                ((BusinessException) exception)
                                        .getErrorCode()
                        ).isEqualTo(ErrorCode.AI_DATA_ERROR)
                );
    }


    @Test
    @DisplayName("국 메뉴 수가 부족하면 실패한다")
    void should_fail_when_soup_menu_is_insufficient() {
        LocalDate date = LocalDate.of(2026, 9, 1);

        AiDietGenerateDto.Command command = command(
                date,
                date,
                2
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L),
                List.of(11L, 12L, 13L, 14L)
        );

        assertThatThrownBy(
                () -> assembler.create(result, command)
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                        assertThat(
                                ((BusinessException) exception)
                                        .getErrorCode()
                        ).isEqualTo(ErrorCode.AI_DATA_ERROR)
                );
    }


    @Test
    @DisplayName("반찬 메뉴 수가 부족하면 실패한다")
    void should_fail_when_side_dish_menu_is_insufficient() {
        LocalDate date = LocalDate.of(2026, 9, 1);

        AiDietGenerateDto.Command command = command(
                date,
                date,
                1
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L),
                List.of(11L)
        );

        assertThatThrownBy(
                () -> assembler.create(result, command)
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception ->
                        assertThat(
                                ((BusinessException) exception)
                                        .getErrorCode()
                        ).isEqualTo(ErrorCode.AI_DATA_ERROR)
                );
    }

    private AiDietGenerateDto.Command command(
            LocalDate startDate,
            LocalDate endDate,
            int dailyMealCount
    ) {
        return AiDietGenerateDto.Command.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dailyMealCount(dailyMealCount)
                .build();
    }


    private AiDietGenerateDto.Result result(
            List<Long> soupMenuIds,
            List<Long> sideDishMenuIds
    ) {
        return AiDietGenerateDto.Result.builder()
                .soupMenuIds(soupMenuIds)
                .sideDishMenuIds(sideDishMenuIds)
                .build();
    }
}
