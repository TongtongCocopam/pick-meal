package kongju.pickmeal.infrastructure.external.recipe.publicdata.service;

import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.mapper.MenuMapper;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.data.info.RecipeInfoRow;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.PublicDataRecipeApiClient;
import kongju.pickmeal.infrastructure.external.recipe.publicdata.data.info.RecipeInfoApiResponse;


@Service
@Transactional
@RequiredArgsConstructor
public class MenuImportService {
    private final MenuMapper menuMapper;
    private final MenuRepository menuRepository;
    private final PublicDataRecipeApiClient publicDataRecipeApiClient;

    /**
     * 메뉴 정보 api를 통해 가져와서 db에 저장
     * @param startIdx 시작
     * @param endIdx 끝
     */
    public void importMenus(int startIdx, int endIdx) {
        RecipeInfoApiResponse response =
                publicDataRecipeApiClient.fetchRecipeInfos(startIdx, endIdx);

        if (response == null || response.grid() == null || response.grid().row() == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_EMPTY_RESPONSE);
        }

        List<Menu> menus = new ArrayList<>();

        for(RecipeInfoRow row : response.grid().row()) {
            if(row.recipeId() == null) {
                continue;
            }

            if(menuRepository.existsByExternalRecipeId(row.recipeId())) {
                continue;
            }

            Menu menu = menuMapper.toMenu(row);
            menus.add(menu);
        }

        menuRepository.saveAll(menus);
    }

}
