package kongju.pickmeal.application.menu.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;

import kongju.pickmeal.infrastructure.external.recipe.foodsafety.FoodImportService;


@Profile("food-safety-import")
@Component
@RequiredArgsConstructor
public class FoodSafetyRecipeImportRunner implements CommandLineRunner {
    private final FoodImportService foodImportService;

    @Override
    public void run(String... args) throws Exception {
        int totalCount = 1146;
        int pageSize = 100;

        for (int start = 1; start <= totalCount; start += pageSize) {
            int end = Math.min(start + pageSize - 1, totalCount);

            foodImportService.importMenusIngredients(start, end);
        }
    }
}
