package kongju.pickmeal.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "external.recipe")
public class RecipeApiProperties {
    private String baseUrl;
    private String apiKey;
    private String type;
    private String recipeInfoApiUrl;
    private String recipeIngredientApiUrl;
}
