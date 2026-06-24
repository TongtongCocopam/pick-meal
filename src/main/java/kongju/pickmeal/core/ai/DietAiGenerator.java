package kongju.pickmeal.core.ai;


public interface DietAiGenerator {
    AiDietGenerateDto.Result generate(AiDietGenerateDto.Command command);
}
