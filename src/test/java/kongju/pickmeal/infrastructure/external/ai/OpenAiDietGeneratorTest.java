
package kongju.pickmeal.infrastructure.external.ai;

import java.util.List;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;

import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.infrastructure.external.ai.data.OpenAiClient;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class OpenAiDietGeneratorTest {

    @Mock
    private OpenAiClient openAiClient;

    @Mock
    private OpenAiPromptBuilder promptBuilder;

    private ObjectMapper objectMapper;
    private OpenAiDietGenerator generator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        generator = new OpenAiDietGenerator(
                openAiClient,
                promptBuilder,
                objectMapper
        );

        given(promptBuilder.system()).willReturn("system prompt");

        given(promptBuilder.user(any(AiDietGenerateDto.Command.class))).willReturn("user prompt");
    }

    @Test
    @DisplayName("AI 응답 JSON을 식단 결과 DTO로 변환한다")
    void shouldParseAiResponse() {
        AiDietGenerateDto.Command command = createCommand();

        mockAiResponse("""
                {
                    "soupMenuIds": [1, 2],
                    "sideDishMenuIds": [10, 11, 12, 13]
                }
                """);

        AiDietGenerateDto.Result result = generator.generate(command);

        assertThat(result.soupMenuIds()).containsExactly(1L, 2L);

        assertThat(result.sideDishMenuIds()).containsExactly(10L, 11L, 12L, 13L);
    }

    @Test
    @DisplayName("AI 응답이 잘못된 JSON이면 예외가 발생한다")
    void shouldThrowWhenAiResponseIsInvalidJson() {
        AiDietGenerateDto.Command command = createCommand();

        mockAiResponse("이건 JSON이 아닙니다.");

        assertThatThrownBy(() -> generator.generate(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI 프롬프트 데이터 JSON 변환에 실패했습니다.");
    }

    @Test
    @DisplayName("식단 기간과 식사 수에 맞는 JSON Schema를 생성한다")
    void shouldCreateResponseSchemaWithRequiredMealCount()
            throws Exception {

        // 3일간 하루 2끼
        AiDietGenerateDto.Command command = createCommand(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                2
        );

        mockAiResponse("""
                {
                    "soupMenuIds": [1, 2, 3, 4, 5, 6],
                    "sideDishMenuIds": [
                        10, 11, 12, 13, 14, 15,
                        16, 17, 18, 19, 20, 21
                    ]
                }
                """);

        generator.generate(command);

        ArgumentCaptor<OpenAiChatOptions> captor = ArgumentCaptor.forClass(OpenAiChatOptions.class);

        verify(openAiClient).request(anyString(), anyString(), captor.capture());

        ResponseFormat responseFormat = captor.getValue().getResponseFormat();

        assertThat(responseFormat.getType()).isEqualTo(ResponseFormat.Type.JSON_SCHEMA);

        JsonNode schema = objectMapper.valueToTree(responseFormat.getJsonSchema().getSchema());

        assertThat(schema.at("/properties/soupMenuIds/minItems").asInt()).isEqualTo(6);

        assertThat(schema.at("/properties/soupMenuIds/maxItems").asInt()).isEqualTo(6);

        assertThat(schema.at("/properties/sideDishMenuIds/minItems").asInt()).isEqualTo(12);

        assertThat(schema.at("/properties/sideDishMenuIds/maxItems").asInt()).isEqualTo(12);
    }

    @Test
    @DisplayName("생성한 프롬프트와 옵션을 OpenAiClient에 전달한다")
    void shouldDelegateRequestToOpenAiClient() {
        AiDietGenerateDto.Command command = createCommand();

        mockAiResponse("""
                {
                    "soupMenuIds": [1, 2],
                    "sideDishMenuIds": [10, 11, 12, 13]
                }
                """);

        generator.generate(command);

        verify(promptBuilder).user(command);

        verify(openAiClient).request(eq("system prompt"), eq("user prompt"), any(OpenAiChatOptions.class));
    }

    // 공통 AI 응답 Mock
    private void mockAiResponse(String json) {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);

        given(response.getResult().getOutput().getText()).willReturn(json);

        given(response.getMetadata().getUsage()).willReturn(mock(Usage.class));

        given(openAiClient.request(anyString(), anyString(), any(OpenAiChatOptions.class))).willReturn(response);
    }

    // 기본 테스트 명령
    private AiDietGenerateDto.Command createCommand() {
        return createCommand(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1),
                2
        );
    }

    // 날짜와 식사 수를 지정하는 테스트 명령
    private AiDietGenerateDto.Command createCommand(
            LocalDate startDate,
            LocalDate endDate,
            int dailyMealCount
    ) {
        return AiDietGenerateDto.Command.builder()
                .userId(1L)
                .startDate(startDate)
                .endDate(endDate)
                .dailyMealCount(dailyMealCount)
                .menuCandidates(List.of())
                .userMenus(List.of())
                .userMenuPicks(List.of())
                .healthConditions(List.of())
                .disease(List.of())
                .preferredIngredients(List.of())
                .dislikedIngredients(List.of())
                .allergyIngredients(List.of())
                .build();
    }
}
