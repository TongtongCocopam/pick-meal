package kongju.pickmeal.application.diet.data;

import lombok.Builder;

import java.util.List;

public class MenuPickDto {
    @Builder
    public record CreateRequest(
            List<Long> menuIds
    ){}

    @Builder
    public record CreateResponse(
            Integer pickedCount,
            List<itemResponse> items
    ){}

    @Builder
    public record itemResponse(
            Long pickId,
            Long menuId,
            String menuName
    ){}
}

// 메뉴 선택권 개수 체크하여 판단