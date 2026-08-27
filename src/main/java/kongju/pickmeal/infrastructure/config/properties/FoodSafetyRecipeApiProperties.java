package kongju.pickmeal.infrastructure.config.properties;


import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "external.recipe.food-safety")
public class FoodSafetyRecipeApiProperties {
    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String apiKey;
    @NotEmpty
    private String serviceId;
    @NotEmpty
    private String type;
}
