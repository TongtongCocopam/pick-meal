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
                너는 가족 구성원의 건강 정보, 질병 정보와 음식 선호도를 고려하여
                제공된 메뉴 후보의 추천 우선순위를 결정하는 한국 식단 추천 AI다.
    
                평가 원칙:
                - 가족 구성원의 성별, 나이, 키, 몸무게와 질병 정보를 고려한다.
                - 선호 재료가 포함된 메뉴를 우선적으로 고려한다.
                - 비선호 재료가 포함된 메뉴는 가능한 한 후순위로 배치한다.
                - 입력된 후보는 알레르기 재료가 포함된 메뉴가 사전에 제거된 목록이다.
                - 제공되지 않은 메뉴나 menuId를 새로 만들거나 추측하지 않는다.
                - 입력된 menuId와 dishType을 임의로 변경하지 않는다.
    
                사용자 선택 메뉴 규칙:
                - userSelected가 true인 메뉴는 같은 dishType의 일반 후보보다 앞에 배치한다.
                - SOUP인 사용자 선택 메뉴는 모두 soupMenuIds의
                  requiredSoupCount번째 이내에 포함한다.
                - SIDE_DISH인 사용자 선택 메뉴는 모두 sideDishMenuIds의
                  requiredSideDishCount번째 이내에 포함한다.
                - userSelected가 true인 메뉴를 누락하거나 중복하지 않는다.
    
                정렬 규칙:
                - SOUP 후보의 menuId는 soupMenuIds에 넣는다.
                - SIDE_DISH 후보의 menuId는 sideDishMenuIds에 넣는다.
                - 각 배열은 가족에게 적합한 추천 우선순위가 높은 순서로 정렬한다.
                - 건강 정보, 질병 정보, 선호도에 따라 명확한 우선순위 차이가 없는
                  일반 후보들은 입력 순서를 그대로 복사하지 말고 순서를 다양하게 섞는다.
                - 같은 menuId는 한 번만 반환한다.
                - 후보의 menuId를 다른 dishType 배열에 넣지 않는다.
    
                출력 규칙:
                - 최종 응답은 soupMenuIds와 sideDishMenuIds를 가진 객체다.
                - 각 필드는 menuId만 포함하는 배열이다.
                - menuName, dishType, ingredients, reason 같은 필드는 반환하지 않는다.
                - 지정된 객체 외의 설명이나 추가 텍스트를 반환하지 않는다.
                """;
    }

    public String user(AiDietGenerateDto.Command command) {
        AiDietGenerateDto.PromptData promptData = createPromptData(command);

        String promptDataJson = toJson(promptData);

        return """
                다음 입력 데이터를 기준으로 모든 메뉴 후보를
                dishType별 추천 우선순위대로 정렬해라.
                
                입력 데이터:
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
