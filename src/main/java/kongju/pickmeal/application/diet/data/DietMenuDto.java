package kongju.pickmeal.application.diet.data;

import java.util.List;
import java.math.BigDecimal;

import lombok.Builder;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;


public class DietMenuDto {
    @Builder
    public record ReplaceRequest(
            Long menuId
    ) {
    }

    @Builder
    public record ReplaceResponse(
            Long replacedMenuId,
            String menuName
    ) {
    }

    @Builder
    public record ReplacementMenuListResponse(
            Long dietId,
            String keyword,
            DishType dishType,
            List<ReplacementMenuResponse> menus,
            PageInfoResponse pageInfo
    ) {
    }

    @Builder
    public record ReplacementMenuResponse(
            Long menuId,
            String menuName,
            BigDecimal kcal
    ) {
        public static ReplacementMenuResponse from(Menu menu) {
            return ReplacementMenuResponse.builder()
                    .menuId(menu.getId())
                    .menuName(menu.getMenuName())
                    .kcal(menu.getKcal())
                    .build();
        }
    }

    @Builder
    public record PageInfoResponse(
            Integer currentPage,
            Integer totalPages,
            Long totalElements
    ){}

}
