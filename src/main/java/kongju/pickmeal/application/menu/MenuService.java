package kongju.pickmeal.application.menu;

import java.util.List;
import java.util.Arrays;

import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.application.menu.data.MenuDto;
import kongju.pickmeal.application.menu.data.MenuFilterDto;
import kongju.pickmeal.core.menu.repository.MenuRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuIngredientRepository menuIngredientRepository;

    public MenuFilterDto.MetadataResponse getFilterMetadata() {
        List<MenuFilterDto.CategoryResponse> categories = Arrays.stream(MenuCategory.values())
                .map(category -> MenuFilterDto.CategoryResponse.builder()
                        .name(category)
                        .displayName(category.getDisplayName())
                        .build()
                )
                .toList();

        List<MenuFilterDto.DishTypeResponse> dishTypes = Arrays.stream(DishType.values())
                .map(dishType -> MenuFilterDto.DishTypeResponse.builder()
                        .name(dishType)
                        .displayName(dishType.getDisplayName())
                        .build()
                )
                .toList();

        return MenuFilterDto.MetadataResponse.builder()
                .categories(categories)
                .dishTypes(dishTypes)
                .build();
    }


    public MenuDto.ListItemResponse searchMenus(
            MenuCategory category, DishType dishType, Pageable pageable
    ) {
        // 카테고리나 디쉬 타입이 있다면 쿼리로 불러오기
        Page<Menu> menuPage = menuRepository.searchByFilters(category, dishType, pageable);

        List<MenuDto.MenuInfoResponse> menuInfoList = menuPage.stream()
                .map(menu -> MenuDto.MenuInfoResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getMenuName())
                        .category(menu.getCategory())
                        .dishType(menu.getDishType())
                        .kcal(menu.getKcal())
                        .build()
                )
                .toList();

        MenuDto.PageInfoResponse pageInfo = MenuDto.PageInfoResponse.builder()
                .currentPage(menuPage.getNumber() + 1)
                .totalPages(menuPage.getTotalPages())
                .totalElements(menuPage.getTotalElements())
                .build();

        return MenuDto.ListItemResponse.builder()
                .content(menuInfoList)
                .pageInfo(pageInfo)
                .build();
    }

    public MenuDto.DetailResponse detailMenu(Long menuId) {
        // id로 메뉴 찾기
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_MENU_ID));

        List<MenuIngredient> menuIngredients = menuIngredientRepository.findAllByMenuWithIngredient(menu);

        // 메뉴 재료 정보 제공
        List<MenuDto.IngredientResponse> ingredients = menuIngredients.stream()
                .map(menuIngredient -> MenuDto.IngredientResponse.builder()
                        .ingredientName(menuIngredient.getIngredient().getName())
                        .quantityText(menuIngredient.getQuantityText())
                        .build()
                )
                .toList();

        // 메뉴 영양 정보
        return MenuDto.DetailResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .category(menu.getCategory())
                .dishType(menu.getDishType())
                .kcal(menu.getKcal())
                .carbs(menu.getCarbs())
                .fat(menu.getFat())
                .sodium(menu.getSodium())
                .protein(menu.getProtein())
                .ingredients(ingredients)
                .build();
    }

}
