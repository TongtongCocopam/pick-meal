package kongju.pickmeal.application.diet.data;

import lombok.Builder;

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
}
