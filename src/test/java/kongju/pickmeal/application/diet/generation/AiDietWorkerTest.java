package kongju.pickmeal.application.diet.generation;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDate;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import kongju.pickmeal.core.ai.DietAiGenerator;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static kongju.pickmeal.support.fixture.FamilyFixture.family;


@ExtendWith(MockitoExtension.class)
public class AiDietWorkerTest {
    @Mock
    private DietGenerationRepository dietGenerationRepository;
    @Mock
    private AiDietPreparationService preparationService;
    @Mock
    private DietAiGenerator dietAiGenerator;
    @Mock
    private AiDietResultValidator resultValidator;
    @Mock
    private AiDietMealPlanAssembler mealPlanAssembler;
    @Mock
    private AiDietResultSaver resultSaver;
    @InjectMocks
    private AiDietWorker aiDietWorker;

    @Test
    @DisplayName("식단 생성 정보를 찾을 수 없으면 실패")
    void should_fail_when_generation_not_found() {
        UUID generationId = UUID.randomUUID();

        given(dietGenerationRepository.findById(generationId)).willReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> aiDietWorker.generate(
                        1L,
                        generationId,
                        mock(DietGenerationDto.GenerateRequest.class),
                        LocalDate.now(),
                        LocalDate.now().plusDays(6),
                        List.of()
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);

        verifyNoInteractions(dietAiGenerator);
//        verifyNoInteractions(dietRepository);
    }


}
