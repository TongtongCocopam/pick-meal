package kongju.pickmeal.application.diet.generation;

import java.util.List;
import java.util.Arrays;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;


public class AiDietResultValidatorTest {

    private final AiDietResultValidator validator =  new AiDietResultValidator();

    @Test
    @DisplayName("메뉴 개수와 후보 정보가 모두 정상이라면 검증에 성공")
    void should_validate_success() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(11L, DishType.SIDE_DISH),
                        candidate(12L, DishType.SIDE_DISH)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L),
                List.of(11L, 12L)
        );

        assertDoesNotThrow(
                () -> validator.validate(result, command)
        );
    }


    @Test
    @DisplayName("국 메뉴 개수가 필요한 개수보다 부족하면 실패")
    void should_fail_when_soup_count_is_less_than_required() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2),
                List.of()
        );

        // 필요 soup=2, sideDish=4
        AiDietGenerateDto.Result result = result(
                List.of(1L),
                List.of(11L, 12L, 13L, 14L)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("국 메뉴 개수가 필요한 개수보다 많으면 실패")
    void should_fail_when_soup_count_exceeds_required() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of()
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 2L),
                List.of(11L, 12L)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("반찬 메뉴 개수가 필요한 개수와 다르면 실패")
    void should_fail_when_side_dish_count_is_invalid() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of()
        );

        // 필요 sideDish=2
        AiDietGenerateDto.Result result = result(
                List.of(1L),
                List.of(11L)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("동일한 menuId가 중복되면 실패")
    void should_fail_when_menu_id_is_duplicated() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2),
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(2L, DishType.SOUP),

                        candidate(11L, DishType.SIDE_DISH),
                        candidate(12L, DishType.SIDE_DISH),
                        candidate(13L, DishType.SIDE_DISH),
                        candidate(14L, DishType.SIDE_DISH)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 1L),
                List.of(11L, 12L, 13L, 14L)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("후보에 존재하지 않는 menuId가 있으면 실패")
    void should_fail_when_menu_id_is_not_candidate() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(11L, DishType.SIDE_DISH),
                        candidate(12L, DishType.SIDE_DISH)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(999L),
                List.of(11L, 12L)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("SOUP 배열에 SIDE_DISH 메뉴가 포함되면 실패")
    void should_fail_when_dish_type_is_invalid() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of(
                        candidate(1L, DishType.SIDE_DISH),
                        candidate(11L, DishType.SIDE_DISH),
                        candidate(12L, DishType.SIDE_DISH)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L),
                List.of(11L, 12L)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MENU_DATA);
    }


    @Test
    @DisplayName("menuId가 null이면 실패")
    void should_fail_when_menu_id_is_null() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(11L, DishType.SIDE_DISH),
                        candidate(12L, DishType.SIDE_DISH)
                )
        );

        AiDietGenerateDto.Result result = result(
                Arrays.asList((Long) null),
                List.of(11L, 12L)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("AI 결과가 null이면 실패")
    void should_fail_when_result_is_null() {
        AiDietGenerateDto.Command command = command(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                List.of()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validate(null, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    private AiDietGenerateDto.Command command(
            LocalDate startDate,
            LocalDate endDate,
            List<AiDietGenerateDto.MenuCandidate> candidates
    ) {
        return AiDietGenerateDto.Command.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dailyMealCount(1)
                .menuCandidates(candidates)
                .userMenus(List.of())
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


    private AiDietGenerateDto.MenuCandidate candidate(
            Long menuId,
            DishType dishType
    ) {
        return AiDietGenerateDto.MenuCandidate.builder()
                .menuId(menuId)
                .dishType(dishType)
                .build();
    }
}
