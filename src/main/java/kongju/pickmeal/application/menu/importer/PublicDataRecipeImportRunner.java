package kongju.pickmeal.application.menu.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;

import kongju.pickmeal.infrastructure.external.recipe.publicdata.service.MenuImportService;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.service.IngredientImportService;


@Profile("public-data-import")
@Component
@RequiredArgsConstructor
public class PublicDataRecipeImportRunner implements CommandLineRunner {

    private final MenuImportService menuImportService;
    private final IngredientImportService ingredientImportService;

    @Override
    public void run(String... args) throws Exception {
        menuImportService.importMenus(1,500);

        int totalCount = 180330;
        int pageSize = 500;

        for (int start = 1; start <= totalCount; start += pageSize) {
            int end = Math.min(start + pageSize - 1, totalCount);
            ingredientImportService.importIngredients(start, end);
        }
    }

}
