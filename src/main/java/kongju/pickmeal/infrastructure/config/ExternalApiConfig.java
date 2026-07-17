package kongju.pickmeal.infrastructure.config;

import org.springframework.web.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@Configuration
@EnableConfigurationProperties({
        PublicRecipeApiProperties.class,
        FoodSafetyRecipeApiProperties.class
})
public class ExternalApiConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
