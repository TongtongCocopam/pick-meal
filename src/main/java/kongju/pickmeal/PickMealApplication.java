package kongju.pickmeal;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import kongju.pickmeal.core.auth.RefreshTokenRepository;

@SpringBootApplication
public class PickMealApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickMealApplication.class, args);
    }

}
