package kongju.pickmeal.infrastructure.config;

import org.springframework.web.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ExternalApiConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
