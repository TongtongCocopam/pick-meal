package kongju.pickmeal.application.menu;

import java.util.List;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.application.menu.data.MenuFilterDto;


@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {

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



}
