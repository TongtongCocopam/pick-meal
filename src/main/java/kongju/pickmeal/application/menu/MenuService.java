package kongju.pickmeal.application.menu;

import java.util.List;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.application.menu.data.MenuFilterDto;
import kongju.pickmeal.core.menu.repository.MenuRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

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


    public MenuFilterDto.ListItemResponse searchMenus(
            MenuCategory category, DishType dishType, Pageable pageable
    ) {
        // 카테고리나 디쉬 타입이 있다면 쿼리로 불러오기
        Page<Menu> menuPage = menuRepository.searchByFilters(category, dishType, pageable);

        List<MenuFilterDto.MenuInfo> menuInfoList = menuPage.stream()
                .map(menu -> MenuFilterDto.MenuInfo.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getMenuName())
                        .category(menu.getCategory())
                        .dishType(menu.getDishType())
                        .kcal(menu.getKcal())
                        .build()
                )
                .toList();

        MenuFilterDto.PageInfo pageInfo = MenuFilterDto.PageInfo.builder()
                .currentPage(menuPage.getNumber() + 1)
                .totalPages(menuPage.getTotalPages())
                .totalElements(menuPage.getTotalElements())
                .build();

        return MenuFilterDto.ListItemResponse.builder()
                .content(menuInfoList)
                .pageInfo(pageInfo)
                .build();
    }

}
