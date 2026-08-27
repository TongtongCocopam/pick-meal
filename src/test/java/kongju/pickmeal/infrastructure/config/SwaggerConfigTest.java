package kongju.pickmeal.infrastructure.config;

import org.junit.jupiter.api.Test;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerConfigTest {
    @Test
    void should_configure_bearer_auth() {
        SwaggerConfig config = new SwaggerConfig();

        OpenAPI openAPI = config.openAPI();

        SecurityScheme scheme = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }
}
