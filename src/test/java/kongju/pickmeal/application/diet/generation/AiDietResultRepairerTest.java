package kongju.pickmeal.application.diet.generation;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;


public class AiDietResultRepairerTest {
    private final AiDietResultRepairer repairer = new AiDietResultRepairer();

    @Test
    @DisplayName("중복 메뉴가 없으면 기존 메뉴를 그대로 반환")
    void should_return_same_ids_when_no_duplicates() {
        AiDietGenerateDto.Command command = command(
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(2L, DishType.SOUP),
                        candidate(11L, DishType.SIDE_DISH),
                        candidate(12L, DishType.SIDE_DISH)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 2L),
                List.of(11L, 12L)
        );

        AiDietGenerateDto.Result repaired = repairer.repair(result, command);

        assertThat(repaired.soupMenuIds()).containsExactly(1L, 2L);

        assertThat(repaired.sideDishMenuIds()).containsExactly(11L, 12L);
    }


    @Test
    @DisplayName("국 메뉴 중복 1개는 사용되지 않은 국 후보로 교체")
    void should_repair_one_duplicate_soup() {
        AiDietGenerateDto.Command command = command(
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(2L, DishType.SOUP),
                        candidate(3L, DishType.SOUP)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 1L, 2L),
                List.of()
        );

        AiDietGenerateDto.Result repaired =
                repairer.repair(result, command);

        assertThat(repaired.soupMenuIds())
                .containsExactly(1L, 3L, 2L);

        assertThat(repaired.soupMenuIds())
                .doesNotHaveDuplicates();
    }


    @Test
    @DisplayName("반찬 메뉴 중복 3개까지 사용되지 않은 반찬 후보로 교체")
    void should_repair_three_duplicate_side_dishes() {
        AiDietGenerateDto.Command command = command(
                List.of(
                        candidate(10L, DishType.SIDE_DISH),
                        candidate(11L, DishType.SIDE_DISH),
                        candidate(12L, DishType.SIDE_DISH),
                        candidate(13L, DishType.SIDE_DISH),
                        candidate(14L, DishType.SIDE_DISH),
                        candidate(15L, DishType.SIDE_DISH)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(),
                List.of(
                        10L, 10L,
                        11L, 11L,
                        12L, 12L
                )
        );

        AiDietGenerateDto.Result repaired = repairer.repair(result, command);

        assertThat(repaired.sideDishMenuIds())
                .containsExactly(
                        10L, 13L,
                        11L, 14L,
                        12L, 15L
                );

        assertThat(repaired.sideDishMenuIds()).doesNotHaveDuplicates();
    }


    @Test
    @DisplayName("중복 메뉴가 3개를 초과하면 실패")
    void should_fail_when_duplicate_count_exceeds_limit() {
        AiDietGenerateDto.Command command = command(
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(2L, DishType.SOUP),
                        candidate(3L, DishType.SOUP),
                        candidate(4L, DishType.SOUP),
                        candidate(5L, DishType.SOUP)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(
                        1L,
                        1L,
                        1L,
                        1L,
                        1L
                ),
                List.of()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairer.repair(result, command)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("중복 메뉴를 교체할 후보가 부족하면 실패")
    void should_fail_when_replacement_candidate_is_insufficient() {
        AiDietGenerateDto.Command command = command(
                List.of(
                        candidate(1L, DishType.SOUP)
                )
        );

        AiDietGenerateDto.Result result = result(
                List.of(1L, 1L),
                List.of()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairer.repair(result, command)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("AI 응답이 null이면 실패")
    void should_fail_when_result_is_null() {
        AiDietGenerateDto.Command command = command(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairer.repair(null, command)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("AI 응답의 메뉴 배열이 null이면 실패")
    void should_fail_when_result_menu_ids_are_null() {
        AiDietGenerateDto.Command command = command(List.of());

        AiDietGenerateDto.Result result = AiDietGenerateDto.Result.builder()
                .soupMenuIds(null)
                .sideDishMenuIds(List.of())
                .build();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairer.repair(result, command)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    @Test
    @DisplayName("AI 응답에 null menuId가 있으면 실패")
    void should_fail_when_menu_id_is_null() {
        AiDietGenerateDto.Command command = command(
                List.of(
                        candidate(1L, DishType.SOUP),
                        candidate(2L, DishType.SOUP)
                )
        );

        AiDietGenerateDto.Result result = result(
                Arrays.asList(1L, null),
                List.of()
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairer.repair(result, command)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_DATA_ERROR);
    }


    private AiDietGenerateDto.Command command(
            List<AiDietGenerateDto.MenuCandidate> menuCandidates
    ) {
        return AiDietGenerateDto.Command.builder()
                .userMenus(List.of())
                .menuCandidates(menuCandidates)
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
