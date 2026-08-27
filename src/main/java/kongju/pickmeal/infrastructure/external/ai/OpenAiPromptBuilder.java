package kongju.pickmeal.infrastructure.external.ai;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Stream;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import kongju.pickmeal.core.ai.AiDietGenerateDto;


@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiPromptBuilder {
    private final ObjectMapper objectMapper;

    public String system() {
        return """
                You are a Korean meal-planning AI.
                Your task is to arrange the provided menu candidates into the actual chronological order
                in which they will be used in the meal plan, while considering family health information,
                diseases, food preferences, and meal variety.
    
                [Hard Constraints]
                The following rules have higher priority than all recommendation and diversity rules.
    
                - The length of soupMenuIds must be exactly equal to requiredSoupCount.
                - The length of sideDishMenuIds must be exactly equal to requiredSideDishCount.
                - Never return the same menuId more than once.
                - Never create, guess, or return a menuId that does not exist in candidates.
                - SOUP candidates must only be placed in soupMenuIds.
                - SIDE_DISH candidates must only be placed in sideDishMenuIds.
                - Never change a menuId or its dishType.
                - If the meal composition or diversity rules cannot all be satisfied,
                  relax those rules instead of duplicating or omitting menuIds.
    
                [User-Selected Menus]
                - Menus with userSelected=true must be included in the result for their dishType.
                - Prefer placing user-selected menus earlier than regular candidates when appropriate.
                - Never duplicate a user-selected menu.
    
                [Family Suitability]
                - Consider each family member's gender, age, height, weight, and diseases.
                - Prefer menus containing preferred ingredients.
                - Give lower priority to menus containing disliked ingredients when possible.
                - Candidates containing allergy ingredients have already been removed before this request.
    
                [Meal Placement]
                - The order of the returned arrays represents actual chronological meal-plan order,
                  not a ranking-score order.
                - menuIds are consumed sequentially from startDate to endDate.
                - Each meal uses exactly 1 SOUP and 2 SIDE_DISH menus.
                - sideDishMenuIds are consumed in consecutive pairs:
                  indices 0-1 form one meal, 2-3 form the next meal, and so on.
                - Each day contains dailyMealCount meals.
    
                [Meal Composition and Diversity]
                Apply the following rules whenever possible without violating the Hard Constraints.
    
                - Do not place menus with similar names or similar main ingredients on the same day.
                - Avoid placing similar menus again on the following day when possible.
                - Distribute menus sharing the same main ingredient across the full meal-plan period.
    
                - Among the two SIDE_DISH menus in one meal, preferably use at most one meat-based menu.
                - Treat menus whose main ingredient is beef, pork, chicken, duck, or similar meat
                  as meat-based menus.
                - When one meat-based SIDE_DISH is used, prefer a second SIDE_DISH based on vegetables,
                  tofu, mushrooms, seaweed, eggs, or another non-meat main ingredient.
    
                - Distribute chicken-based menus, including chicken breast, chicken thigh,
                  and other chicken-centered dishes, so that they appear no more than once
                  within the same 7-day period whenever possible.
    
                [Output]
                - Return an object containing only soupMenuIds and sideDishMenuIds.
                - Each array must contain numeric menuId values only.
                - Do not return menuName, dishType, ingredients, reason, or any other fields.
                - Do not return explanations, comments, markdown, or any additional text.
                """;
    }

    public String user(AiDietGenerateDto.Command command) {
        AiDietGenerateDto.PromptData promptData = createPromptData(command);

        String promptDataJson = toJson(promptData);

        return """
                Analyze the following input and select menus for the meal plan.
                
                - Return exactly requiredSoupCount SOUP IDs.
                - Return exactly requiredSideDishCount SIDE_DISH IDs.
                - Order all IDs chronologically for actual meal placement.
                - Use candidate menuIds only.
        
                Input:
                %s
                """.formatted(promptDataJson);
    }

    private AiDietGenerateDto.PromptData createPromptData(
            AiDietGenerateDto.Command command
    ) {
        Set<Long> seenMenuIds = new HashSet<>();

        List<AiDietGenerateDto.RankCandidate> candidates = Stream.concat(
                        // 사용자 선택 메뉴를 먼저 배치
                        command.userMenus().stream()
                                .map(menu -> AiDietGenerateDto.RankCandidate.builder()
                                        .menuId(menu.menuId())
                                        .menuName(menu.menuName())
                                        .dishType(menu.dishType())
                                        .ingredients(menu.ingredients())
                                        .userSelected(true)
                                        .build()),

                        // 일반 후보는 나중에 배치
                        command.menuCandidates().stream()
                                .map(menu -> AiDietGenerateDto.RankCandidate.builder()
                                        .menuId(menu.menuId())
                                        .menuName(menu.menuName())
                                        .dishType(menu.dishType())
                                        .ingredients(menu.ingredients())
                                        .userSelected(false)
                                        .build())
                )
                .filter(candidate -> seenMenuIds.add(candidate.menuId()))
                .toList();

        log.info("AI 전달 후보 개수: {}", (long) candidates.size());

        int totalMealCount = calculateTotalMealCount(command.startDate(), command.endDate(), command.dailyMealCount());

        return AiDietGenerateDto.PromptData.builder()
                .candidates(candidates)
                .healthConditions(command.healthConditions())
                .diseases(command.disease())
                .preferredIngredients(command.preferredIngredients())
                .dislikedIngredients(command.dislikedIngredients())
                .requiredSoupCount(totalMealCount)
                .requiredSideDishCount(totalMealCount * 2)
                .build();
    }

    /**
     * 식단 개수 계산
     * @param startDate 시작
     * @param endDate 종료
     * @param dailyMealCount 하루 식단 개수
     * @return 식단 개수
     */
    private int calculateTotalMealCount(LocalDate startDate, LocalDate endDate, int dailyMealCount
    ) {
        int dayCount = Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate) + 1);

        return dayCount * dailyMealCount;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("AI 프롬프트 데이터 JSON 변환에 실패했습니다.", e);
        }
    }
}
