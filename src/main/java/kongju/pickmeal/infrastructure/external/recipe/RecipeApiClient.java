package kongju.pickmeal.infrastructure.external.recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kongju.pickmeal.infrastructure.config.RecipeApiProperties;
import kongju.pickmeal.infrastructure.external.recipe.data.info.RecipeInfoApiResponse;
import kongju.pickmeal.infrastructure.external.recipe.data.ingredient.RecipeIngredientApiResponse;


/**
 * 외부 api호출 담당
 * dto응답 반환
 */
@Component
@RequiredArgsConstructor
public class RecipeApiClient {
    private final RestClient restClient;
    private final RecipeApiProperties properties;

    // get요청 보내기
    public RecipeInfoApiResponse fetchRecipeInfos(int startIdx, int endIdx) {
        return restClient.get()
                // URL만들기
                .uri("{baseUrl}/{apiKey}/{type}/{apiUrl}/{start}/{end}",
                        properties.getBaseUrl(),
                        properties.getApiKey(),
                        properties.getType(),
                        properties.getRecipeInfoApiUrl(),
                        startIdx,
                        endIdx)
                .retrieve()
                .body(RecipeInfoApiResponse.class);
    }

    // 응답 받기
    public RecipeIngredientApiResponse fetchRecipeIngredients(int startIdx, int endIdx) {
        return restClient.get()
                .uri("{baseUrl}/{apiKey}/{type}/{apiUrl}/{start}/{end}",
                        properties.getBaseUrl(),
                        properties.getApiKey(),
                        properties.getType(),
                        properties.getRecipeIngredientApiUrl(),
                        startIdx,
                        endIdx)
                .retrieve()
                .body(RecipeIngredientApiResponse.class);
    }
}
