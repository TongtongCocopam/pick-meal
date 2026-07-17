package kongju.pickmeal.infrastructure.external.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.ai.AiDietGenerateDto;


@Component
@RequiredArgsConstructor
public class OpenAiPromptBuilder {
    public String system() {
        return """
                너는 가족 구성원의 선호, 건강 정보, 질병 정보를 고려해 식단을 구성하는 한국 영양사 AI다.
                
                핵심 규칙:
                - 반드시 제공된 menuId만 사용한다.
                - 제공되지 않은 메뉴, 재료, menuId를 절대 생성하지 않는다.
                - 응답은 반드시 지정된 JSON 구조로만 반환한다.
                - 설명 문장, 마크다운, 코드블록은 절대 포함하지 않는다.
                - 알러지 재료가 포함된 메뉴는 절대 선택하지 않는다.
                - 기본 식단 베이스는 한식이다.
                - 단, 가족 선호 메뉴나 요청에 따라 한식 외 메뉴도 일부 포함할 수 있다.
                - 가족 구성원의 나이, 건강 정보, 질병을 고려해 지나치게 자극적이거나 건강에 좋지 않은 메뉴는 최소화한다.
                - 모든 가족의 요구를 완벽히 만족할 수 없다면 건강상 안전성과 균형을 우선한다.
                
                식단 구성 규칙:
                - 각 mealPlan은 하나의 식사 단위다.
                - 각 식사는 제공된 메뉴 후보의 dishType을 기준으로 구성한다.
                - MAIN_DISH는 메인 메뉴다.
                - SOUP은 국 또는 찌개류다.
                - SIDE_DISH는 반찬이다.
                - 메인 메뉴가 충분히 식사 역할을 할 수 있다면 MAIN_DISH 1개만 선택할 수 있다.
                - 기본 반찬은 3가지를 권장한다.
                - 단, 메인 메뉴가 있는 경우 반찬은 1~3가지로 줄일 수 있다.
                - SOUP이 포함되는 식사에는 반드시 SIDE_DISH를 2개 이상 포함해야 한다.
                - SOUP 단독 식사는 금지한다.
                - SIDE_DISH만으로 구성된 식사는 금지한다.
                - 같은 mealPlan 안에서 같은 menuId를 중복 사용하지 않는다.
                - 전체 기간 안에서 같은 메뉴가 과도하게 반복되지 않도록 한다.
                
                출력 규칙:
                - startDate와 endDate는 입력받은 기간과 동일해야 한다.
                - mealPlans는 입력받은 기간 안의 식사만 포함해야 한다.
                - 각 menuItems에는 menuId만 포함한다.
                - menuName, dishType, reason 같은 추가 필드는 반환하지 않는다.
                """;
    }

    public String user(AiDietGenerateDto.Command command) {
        return """
                다음 조건에 맞춰 가족 식단을 생성해라.
                
                생성 기간:
                %s ~ %s
                
                선호 재료:
                %s
                
                비선호 재료:
                %s
                
                알러지 재료:
                %s
                
                가족 건강 정보:
                %s
                
                가족 질병 정보:
                %s
                
                가족이 직접 선택한 메뉴:
                %s
                
                사용 가능한 메뉴 후보:
                %s
                
                반드시 아래 JSON 구조로만 응답해라.
                
                {
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-31",
                  "mealPlans": [
                    {
                      "date": "2026-07-01",
                      "mealType": "BREAKFAST",
                      "menuItems": [
                        {
                          "menuId": 1
                        },
                        {
                          "menuId": 2
                        },
                        {
                          "menuId": 3
                        }
                      ]
                    },
                    {
                      "date": "2026-07-01",
                      "mealType": "DINNER",
                      "menuItems": [
                        {
                          "menuId": 10
                        }
                      ]
                    }
                  ]
                }
                """.formatted(
                command.startDate(),
                command.endDate(),
                command.preferredIngredients(),
                command.dislikedIngredients(),
                command.allergyIngredients(),
                command.healthConditions(),
                command.disease(),
                command.userMenuPicks(),
                command.menuCandidates()
        );
    }

}
