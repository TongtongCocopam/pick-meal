package kongju.pickmeal.infrastructure.external.recipe.publicdata;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import org.springframework.web.client.RestClientResponseException;
import kongju.pickmeal.infrastructure.config.PublicRecipeApiProperties;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.data.info.RecipeInfoApiResponse;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.data.ingredient.RecipeIngredientApiResponse;


/**
 * 외부 api호출 담당
 * dto응답 반환
 */
@Component
@RequiredArgsConstructor
public class PublicDataRecipeApiClient {
    private final RestClient restClient;
    private final PublicRecipeApiProperties properties;

    /**
     * 레시피 기본 정보 가져오기
     * @param startIdx 시작 인덱스
     * @param endIdx 종료 인덱스
     * @return dto List
     */
    public RecipeInfoApiResponse fetchRecipeInfos(int startIdx, int endIdx) {
        String url = "%s/%s/%s/%s/%d/%d".formatted(
                properties.getBaseUrl(),
                properties.getApiKey(),
                properties.getType(),
                properties.getRecipeInfoApiUrl(),
                startIdx,
                endIdx
        );
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(RecipeInfoApiResponse.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * 레시피 재료 정보 가져오기
     * @param startIdx 시작 인덱스
     * @param endIdx 종료 인덱스
     * @return dto List
     */
    public RecipeIngredientApiResponse fetchRecipeIngredients(int startIdx, int endIdx) {
        String url = "%s/%s/%s/%s/%d/%d".formatted(
                properties.getBaseUrl(),
                properties.getApiKey(),
                properties.getType(),
                properties.getRecipeIngredientApiUrl(),
                startIdx,
                endIdx
        );
        try {

            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(RecipeIngredientApiResponse.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);

        }
    }
}
