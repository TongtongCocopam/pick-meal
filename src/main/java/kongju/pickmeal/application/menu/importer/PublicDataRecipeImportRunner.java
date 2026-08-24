package kongju.pickmeal.application.menu.importer;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.service.MenuImportService;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.service.IngredientImportService;

@Slf4j
@Profile("public-data-import")
@Component
@RequiredArgsConstructor
public class PublicDataRecipeImportRunner implements CommandLineRunner {

    private final MenuImportService menuImportService;
    private final IngredientImportService ingredientImportService;

    @Override
    public void run(String... args) {
        menuImportService.importMenus(1,500);

        int totalCount = 180330;
        int pageSize = 500;

        for (int start = 1; start <= totalCount; start += pageSize) {
            int end = Math.min(start + pageSize - 1, totalCount);
            try {
                ingredientImportService.importIngredients(start, end);
            }catch (Exception e){
                log.error("public data import 실패 : start={}, end={}", start, end, e);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }
        }
    }

}
