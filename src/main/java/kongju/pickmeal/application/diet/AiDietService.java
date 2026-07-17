package kongju.pickmeal.application.diet;

import java.util.*;
import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;


@Service
@RequiredArgsConstructor
public class AiDietService {
    private final AiDietWorker aiDietWorker;

    /**
     * 비동기 식단 생성 실행
     * @param userId 유저 id
     * @param request 요청 날짜, 끼니 정보
     */
    @Async
    public void generateDietAsync(
            Long userId,
            UUID generationId,
            DietGenerationDto.GenerateRequest request,
            LocalDate startDate,
            LocalDate endDate
    ) {
        aiDietWorker.generate(userId, generationId, request, startDate, endDate);
    }


}
