package kongju.pickmeal.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "external.recipe.public-data")
public class PublicRecipeApiProperties {
    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String apiKey;
    @NotEmpty
    private String type;
    @NotEmpty
    private String recipeInfoApiUrl;
    @NotEmpty
    private String recipeIngredientApiUrl;
}
