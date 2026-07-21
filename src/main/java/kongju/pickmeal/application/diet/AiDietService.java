package kongju.pickmeal.application.diet;

import java.util.*;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiDietService {
    private final AiDietWorker aiDietWorker;
    private final DietGenerationFailureHandler dietGenerationFailureHandler;

    /**
     * 비동기 식단 생성 실행
     *
     * @param userId  유저 id
     * @param request 요청 날짜, 끼니 정보
     */
    @Async
    public void generateDietAsync(
            Long userId,
            UUID generationId,
            DietGenerationDto.GenerateRequest request,
            LocalDate startDate,
            LocalDate endDate,
            List<Long> userMenuPickIds
    ) {
        try {
            aiDietWorker.generate(userId, generationId, request, startDate, endDate, userMenuPickIds);
        } catch (Exception e) {
            try {
                dietGenerationFailureHandler.handleFailure(generationId, userMenuPickIds);
            } catch (Exception failureException) {
                log.error("식단 생성 실패 처리 중 오류");
            }
            log.error("AI 식단 생성 실패");
        }
    }
}
