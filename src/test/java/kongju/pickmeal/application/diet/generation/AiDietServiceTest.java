package kongju.pickmeal.application.diet.generation;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.YearMonth;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import kongju.pickmeal.application.diet.DietGenerationFailureHandler;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;
import kongju.pickmeal.application.diet.event.DietGenerationRequestedEventDto;


@ExtendWith(MockitoExtension.class)
public class AiDietServiceTest {
    @Mock
    private AiDietWorker aiDietWorker;
    @Mock
    private DietGenerationFailureHandler dietGenerationFailureHandler;
    @InjectMocks
    private AiDietService aiDietService;

    private DietGenerationRequestedEventDto event;

    @BeforeEach
    void setUp() {
        event = DietGenerationRequestedEventDto.builder()
                .userId(1L)
                .generationId(UUID.randomUUID())
                .request(
                        DietGenerationDto.GenerateRequest.builder()
                                .targetMonth(YearMonth.now())
                                .dailyMealCount(2)
                                .build()
                )
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .userMenuPickIds(List.of(1L, 2L))
                .build();
    }

    @Test
    @DisplayName("AI 식단 생성에 성공하면 Worker를 실행한다")
    void should_generate_diet_success() {
        aiDietService.generateDietAsync(event);

        verify(aiDietWorker).generate(
                event.userId(),
                event.generationId(),
                event.request(),
                event.startDate(),
                event.endDate(),
                event.userMenuPickIds()
        );

        verifyNoInteractions(dietGenerationFailureHandler);
    }

    @Test
    @DisplayName("AI 식단 생성에 실패하면 실패 처리를 실행한다")
    void should_handle_failure_when_generation_fails() {
        doThrow(new RuntimeException("AI 생성 실패"))
                .when(aiDietWorker)
                .generate(
                        event.userId(),
                        event.generationId(),
                        event.request(),
                        event.startDate(),
                        event.endDate(),
                        event.userMenuPickIds()
                );

        aiDietService.generateDietAsync(event);

        verify(dietGenerationFailureHandler).handleFailure(
                event.generationId(),
                event.userMenuPickIds()
        );
    }

    @Test
    @DisplayName("실패 처리 중 오류가 발생해도 예외를 전파하지 않는다")
    void should_not_propagate_exception_when_failure_handling_fails() {
        doThrow(new RuntimeException("AI 생성 실패"))
                .when(aiDietWorker)
                .generate(
                        event.userId(),
                        event.generationId(),
                        event.request(),
                        event.startDate(),
                        event.endDate(),
                        event.userMenuPickIds()
                );

        doThrow(new RuntimeException("실패 복구 실패"))
                .when(dietGenerationFailureHandler)
                .handleFailure(
                        event.generationId(),
                        event.userMenuPickIds()
                );

        assertDoesNotThrow(() ->
                aiDietService.generateDietAsync(event)
        );

        verify(aiDietWorker).generate(
                event.userId(),
                event.generationId(),
                event.request(),
                event.startDate(),
                event.endDate(),
                event.userMenuPickIds()
        );

        verify(dietGenerationFailureHandler).handleFailure(
                event.generationId(),
                event.userMenuPickIds()
        );
    }
}
