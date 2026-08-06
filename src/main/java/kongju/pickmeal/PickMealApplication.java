package kongju.pickmeal;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAsync
@SpringBootApplication
public class PickMealApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickMealApplication.class, args);
    }

}
