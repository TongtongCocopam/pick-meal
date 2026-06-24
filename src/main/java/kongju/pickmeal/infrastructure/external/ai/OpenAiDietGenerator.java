package kongju.pickmeal.infrastructure.external.ai;

import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.ChatClient;

import kongju.pickmeal.core.ai.DietAiGenerator;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;


@Component
public class OpenAiDietGenerator implements DietAiGenerator {
    private final ChatClient chatClient;
    private final OpenAiPromptBuilder promptBuilder;

    public OpenAiDietGenerator(
            ChatClient.Builder chatClientBuilder,
            OpenAiPromptBuilder promptBuilder
    ){
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
    }

    @Override
    public AiDietGenerateDto.Result generate(AiDietGenerateDto.Command command) {
        AiDietGenerateDto.Result result = chatClient.prompt()
                .system(promptBuilder.system())
                .user(promptBuilder.user(command))
                .call()
                .entity(AiDietGenerateDto.Result.class);

        if (result == null) {
            throw new BusinessException(ErrorCode.AI_PROCESS_FAILED);
        }
        return result;
    }

}
