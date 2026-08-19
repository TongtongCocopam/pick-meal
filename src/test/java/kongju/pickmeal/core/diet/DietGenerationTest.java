package kongju.pickmeal.core.diet;

import kongju.pickmeal.core.diet.type.DietGenerationStatus;
import kongju.pickmeal.core.family.Family;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static kongju.pickmeal.support.fixture.FamilyFixture.family;
import static org.assertj.core.api.Assertions.assertThat;

public class DietGenerationTest {
    @Test
    @DisplayName("ai 식단 생성 처리 중")
    void should_ai_generation_when_processing() {
        Family family = family();
        DietGeneration dietGeneration = DietGeneration.createPending(
                family,
                LocalDate.now(),
                LocalDate.now(),
                2,
                LocalDate.now()
        );

        dietGeneration.processing();

        assertThat(dietGeneration.getStatus()).isEqualTo(DietGenerationStatus.PROCESSING);
    }
    @Test
    @DisplayName("ai 식단 생성 완료")
    void should_ai_generation_when_completed() {
        Family family = family();
        DietGeneration dietGeneration = DietGeneration.createPending(
                family,
                LocalDate.now(),
                LocalDate.now(),
                2,
                LocalDate.now()
        );

        dietGeneration.completed();

        assertThat(dietGeneration.getStatus()).isEqualTo(DietGenerationStatus.COMPLETED);
    }
    @Test
    @DisplayName("ai 식단 생성 실패")
    void should_ai_generation_when_failed() {
        Family family = family();
        DietGeneration dietGeneration = DietGeneration.createPending(
                family,
                LocalDate.now(),
                LocalDate.now(),
                2,
                LocalDate.now()
        );

        dietGeneration.failed();

        assertThat(dietGeneration.getStatus()).isEqualTo(DietGenerationStatus.FAILED);
    }
}
