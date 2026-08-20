package kongju.pickmeal.application.diet.generation;

import java.util.*;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kongju.pickmeal.core.ai.DietAiGenerator;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AiDietWorker {

    private final AiDietResultSaver aiDietResultSaver;
    private final AiDietResultValidator aiDietResultValidator;
    private final AiDietMealPlanAssembler aiDietMealPlanAssembler;
    private final DietGenerationRepository dietGenerationRepository;
    private final AiDietPreparationService aiDietPreparationService;
    private final AiDietResultRepairer aiDietResultRepairer;
    private final DietAiGenerator dietAiGenerator;

    /**
     * 식단 생성 데이터 전처리, 생성, 반환
     *
     * @param userId       유저 아이디
     * @param generationId 생성 아이디
     * @param request      요청 데이터
     */
    public void generate(
            Long userId,
            UUID generationId,
            DietGenerationDto.GenerateRequest request,
            LocalDate startDate,
            LocalDate endDate,
            List<Long> userMenuPickIds
    ) {
        DietGeneration generation = dietGenerationRepository.findById(generationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        log.info("생성 정보 조회 시작: generationId={}", generationId);
        generation.processing();
        log.info("생성 정보 조회 완료: generationId={}", generationId);
        // 전처리
        AiDietGenerateDto.Command command = aiDietPreparationService.prepare(userId, request, startDate, endDate, userMenuPickIds);
        log.info("GPT API 호출 시작: generationId={}", generationId);
        // ai호출
        AiDietGenerateDto.Result result = dietAiGenerator.generate(command);
        log.info(
                "GPT 응답 메뉴 수: soup={}, sideDish={}",
                result.soupMenuIds().size(),
                result.sideDishMenuIds().size()
        );

        // 중복 1~3개 자동 복구
        result = aiDietResultRepairer.repair(result, command);
        log.info("GPT API 응답 수신: generationId={}", generationId);
        log.info("result={}", result);
        // 검증
        aiDietResultValidator.validate(result, command);
        List<AiDietGenerateDto.MealPlan> mealPlans = aiDietMealPlanAssembler.create(result, command);
        log.info("식단 DB 저장 시작: generationId={}", generationId);
        // 저장
        aiDietResultSaver.save(generation, mealPlans, command);

        // 상태변경
        generation.completed();
        log.info("AI 식단 생성 최종 완료: generationId={}", generationId);
    }

}
