package kongju.pickmeal.application.diet.data;

import java.util.List;
import java.time.YearMonth;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;


public class MenuPickDto {
    @Builder
    public record CreateRequest(
            List<Long> menuIds,
            // 현재 달이나 다음달만 허용
            @NotNull
            @JsonFormat(pattern = "yyyy-MM")
            YearMonth targetMonth
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

    @Builder
    public record DeleteResponse(
            Long menuId
    ){}
}
