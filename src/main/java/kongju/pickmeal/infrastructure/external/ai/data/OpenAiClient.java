package kongju.pickmeal.infrastructure.external.ai.data;

import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;


@Component
public class OpenAiClient {
    private final ChatClient chatClient;
    public OpenAiClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponse request(
            String systemPrompt,
            String userPrompt,
            OpenAiChatOptions options
    ) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(options)
                .call()
                .chatResponse();
    }

}
