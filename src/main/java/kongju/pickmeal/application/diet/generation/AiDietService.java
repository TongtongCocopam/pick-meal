package kongju.pickmeal.application.diet.generation;

import kongju.pickmeal.application.diet.DietGenerationFailureHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kongju.pickmeal.application.diet.event.DietGenerationRequestedEvent;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiDietService {
    private final AiDietWorker aiDietWorker;
    private final DietGenerationFailureHandler dietGenerationFailureHandler;

    /**
     * 비동기 식단 생성 실행
     *
     * @param event ai식단 관련 데이터
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void generateDietAsync(
            DietGenerationRequestedEvent event
    ) {
        try {
            log.info(
                    "generateDietAsync thread={}",
                    Thread.currentThread().getName()
            );
            aiDietWorker.generate(event.userId(), event.generationId(), event.request(), event.startDate(), event.endDate(), event.userMenuPickIds());
        } catch (Exception e) {
            try {
                dietGenerationFailureHandler.handleFailure(event.generationId(), event.userMenuPickIds());
            } catch (Exception failureException) {
                log.error("식단 생성 실패 처리 중 오류", failureException);
            }
            log.error("AI 식단 생성 실패", e);
        }
    }
}
