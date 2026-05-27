package kongju.pickmeal.application.user.data;

import java.util.List;

import lombok.Builder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import kongju.pickmeal.core.user.type.DiseaseName;
import kongju.pickmeal.core.user.type.DiseaseCategory;
import kongju.pickmeal.core.user.type.FoodPreferenceType;


public class UserDietProfileDto {
    @Builder
    public record UpdateDiseaseRequest(
            @NotEmpty(message = "질병 정보를 입력해주세요.")
            @Valid
            List<DiseaseRequest> diseases
    ){
    }

    @Builder
    public record DiseaseRequest(
            @NotNull(message = "질병 분류는 필수입니다.")
            DiseaseCategory category,
            @NotNull(message = "상세 병명은 필수입니다.")
            DiseaseName detailName,
            @Size(max = 255, message = "상세 설명은 255자 이하로 입력해주세요.")
            String description
    ) {
    }

    @Builder
    public record UpdateIngredientPreferenceRequest(
            @NotEmpty(message = "재료 정보를 입력해주세요.")
            @Valid
            List<IngredientPreferenceRequest> preferences
    ){
    }

    @Builder
    public record IngredientPreferenceRequest(
            @NotNull(message = "재료 ID는 필수입니다.")
            @Positive(message = "재료 ID는 양수여야 합니다.")
            Long ingredientId,
            @NotNull(message = "음식 선호 타입은 필수입니다.")
            FoodPreferenceType preference
    ) {
    }
}
