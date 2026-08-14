package kongju.pickmeal.infrastructure.external.recipe.foodsafety;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import kongju.pickmeal.infrastructure.config.properties.FoodSafetyRecipeApiProperties;
import kongju.pickmeal.infrastructure.external.recipe.foodsafety.data.FoodSafetyRecipeInfoResponse;


@Component
@RequiredArgsConstructor
public class FoodSafetyRecipeApiClient {
    private final RestClient restClient;
    private final FoodSafetyRecipeApiProperties properties;

    public FoodSafetyRecipeInfoResponse fetchRecipe(int startIdx, int endIdx) {
        String url = "%s/%s/%s/%s/%d/%d".formatted(
                properties.getBaseUrl(),
                properties.getApiKey(),
                properties.getServiceId(),
                properties.getType(),
                startIdx,
                endIdx
        );
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(FoodSafetyRecipeInfoResponse.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }


}
