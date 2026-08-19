package kongju.pickmeal.core.diet;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.type.DietGenerationStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static kongju.pickmeal.support.fixture.FamilyFixture.family;


public class DietGenerationTest {
    @Test
    @DisplayName("ai작업 처리중")
    void should_ai_generate_when_processing() {
        Family family = family();
        DietGeneration generation = DietGeneration.createPending(
                family,
                LocalDate.now(),
                LocalDate.now(),
                2,
                LocalDate.now()
        );
        generation.processing();

        assertThat(generation.getStatus()).isEqualTo(DietGenerationStatus.PROCESSING);
    }

    @Test
    @DisplayName("ai작업 완료")
    void should_ai_generate_when_completed() {
        Family family = family();
        DietGeneration generation = DietGeneration.createPending(
                family,
                LocalDate.now(),
                LocalDate.now(),
                2,
                LocalDate.now()
        );
        generation.completed();

        assertThat(generation.getStatus()).isEqualTo(DietGenerationStatus.COMPLETED);
    }

    @Test
    @DisplayName("ai작업 실패")
    void should_ai_generate_when_failed() {
        Family family = family();
        DietGeneration generation = DietGeneration.createPending(
                family,
                LocalDate.now(),
                LocalDate.now(),
                2,
                LocalDate.now()
        );
        generation.failed();

        assertThat(generation.getStatus()).isEqualTo(DietGenerationStatus.FAILED);
    }
}
