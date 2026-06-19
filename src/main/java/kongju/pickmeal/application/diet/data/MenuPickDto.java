package kongju.pickmeal.application.diet.data;

import java.util.List;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;


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

    @Builder
    public record UpdateRequest(
            @NotNull
            Long menuId
    ){}

    @Builder
    public record UpdateResponse(
            Long menuId,
            String menuName
    ){}
}
