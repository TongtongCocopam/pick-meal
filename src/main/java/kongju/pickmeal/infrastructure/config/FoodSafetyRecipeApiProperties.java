package kongju.pickmeal.infrastructure.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "external.recipe.food-safety")
public class FoodSafetyRecipeApiProperties {
    private String baseUrl;
    private String apiKey;
    private String serviceId;
    private String type;
}
