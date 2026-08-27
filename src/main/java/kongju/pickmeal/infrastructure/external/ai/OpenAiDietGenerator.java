package kongju.pickmeal.infrastructure.external.ai;

import java.util.Objects;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.metadata.Usage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import com.fasterxml.jackson.core.JsonProcessingException;

import kongju.pickmeal.core.ai.DietAiGenerator;
import kongju.pickmeal.core.ai.AiDietGenerateDto;


@Slf4j
@Component
public class OpenAiDietGenerator implements DietAiGenerator {
    private final ChatClient chatClient;
    private final OpenAiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public OpenAiDietGenerator(
            ChatClient.Builder chatClientBuilder,
            OpenAiPromptBuilder promptBuilder, ObjectMapper objectMapper
    ){
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiDietGenerateDto.Result generate(AiDietGenerateDto.Command command) {
        int requiredSoupCount = calculateTotalMealCount(
                command.startDate(),
                command.endDate(),
                command.dailyMealCount()
        );

        int requiredSideDishCount = requiredSoupCount * 2;

        String jsonSchema = createResponseSchema(
                requiredSoupCount,
                requiredSideDishCount
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .responseFormat(
                        new ResponseFormat(
                                ResponseFormat.Type.JSON_SCHEMA,
                                jsonSchema
                        )
                )
                .build();

        // 결과
        ChatResponse response = chatClient.prompt()
                .system(promptBuilder.system())
                .user(promptBuilder.user(command))
                .options(options)
                .call()
                .chatResponse();

        // 사용량 확인
        Usage usage = Objects.requireNonNull(response).getMetadata().getUsage();

        log.info(
                "GPT 토큰 사용량: input={}, output={}, total={}",
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );

        String content = response
                .getResult()
                .getOutput()
                .getText();

        try {
            return objectMapper.readValue(
                    content,
                    AiDietGenerateDto.Result.class
            );

        } catch (JsonProcessingException e) {
//            throw new BusinessException(ErrorCode.AI_PROCESS_FAILED);
            throw new IllegalStateException(
                    "AI 프롬프트 데이터 JSON 변환에 실패했습니다.",
                    e
            );
        }
    }

    /**
     * 식단 개수 카운트
     * @param startDate 시작
     * @param endDate 끝
     * @param dailyMealCount 하루 식단 수
     * @return 식단 개수
     */
    private int calculateTotalMealCount(
            LocalDate startDate,
            LocalDate endDate,
            int dailyMealCount
    ) {
        int dayCount = Math.toIntExact(
                ChronoUnit.DAYS.between(startDate, endDate) + 1
        );

        return dayCount * dailyMealCount;
    }

    /**
     * 최소 메뉴 식단 프롬프트에 추가
     * @param requiredSoupCount 최소 국
     * @param requiredSideDishCount 최소 반찬
     * @return 메뉴 개수 제한 프롬프트
     */
    private String createResponseSchema(
            int requiredSoupCount,
            int requiredSideDishCount
    ) {
        return """
            {
              "type": "object",
              "properties": {
                "soupMenuIds": {
                  "type": "array",
                  "items": {
                    "type": "integer"
                  },
                  "minItems": %d,
                  "maxItems": %d
                },
                "sideDishMenuIds": {
                  "type": "array",
                  "items": {
                    "type": "integer"
                  },
                  "minItems": %d,
                  "maxItems": %d
                }
              },
              "required": [
                "soupMenuIds",
                "sideDishMenuIds"
              ],
              "additionalProperties": false
            }
            """.formatted(
                requiredSoupCount,
                requiredSoupCount,
                requiredSideDishCount,
                requiredSideDishCount
        );
    }

}
