package kongju.pickmeal.application.user.data;

import java.util.List;

import lombok.Builder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import kongju.pickmeal.core.user.type.DiseaseName;
import kongju.pickmeal.core.user.type.DiseaseCategory;
import kongju.pickmeal.core.user.type.FoodPreferenceType;


public class UserDietProfileDto {
    @Builder
    public record UpdateRequest(
            @Valid
            List<DiseaseRequest> diseases,
            @Valid
            List<IngredientPreferenceRequest> ingredientPreferences
    ) {
    }

    @Builder
    public record DiseaseRequest(
            @NotNull(message = "질병 분류는 필수입니다.")
            DiseaseCategory category,
            @NotBlank(message = "상세 병명은 필수입니다.")
            DiseaseName detailName,
            @Size(max = 255, message = "상세 설명은 255자 이하로 입력해주세요.")
            String description
    ) {
    }

    @Builder
    public record IngredientPreferenceRequest(
            @NotNull(message = "재료 ID는 필수입니다.")
            Long ingredientId,
            @NotNull(message = "음식 선호 타입은 필수입니다.")
            FoodPreferenceType preference
    ) {
    }
}
