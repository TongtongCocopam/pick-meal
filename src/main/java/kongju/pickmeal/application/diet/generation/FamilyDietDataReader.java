package kongju.pickmeal.application.diet.generation;

import java.util.Set;
import java.util.List;
import java.time.Period;
import java.util.HashSet;
import java.time.LocalDate;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.user.UserDisease;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.core.user.UserHealthProfile;
import kongju.pickmeal.core.user.type.FoodPreferenceType;
import kongju.pickmeal.core.user.UserIngredientPreference;
import kongju.pickmeal.application.diet.data.FamilyDietData;
import kongju.pickmeal.core.user.repository.UserHealthRepository;
import kongju.pickmeal.core.user.repository.UserDiseaseRepository;
import kongju.pickmeal.core.user.repository.UserIngredientPreferenceRepository;


@Component
@RequiredArgsConstructor
public class FamilyDietDataReader {
    private final UserHealthRepository userHealthRepository;
    private final UserDiseaseRepository userDiseaseRepository;
    private final UserIngredientPreferenceRepository userIngredientPreferenceRepository;

    public FamilyDietData read(List<User> users) {
        // 질병
        List<AiDietGenerateDto.Disease> diseases = getFamilyDiseases(users);
        // 건강
        List<AiDietGenerateDto.HealthCondition> healthConditions = getFamilyHealthConditions(users);
        // 재료
        IngredientPreferenceSummary preferences = getIngredientPreferenceSummary(users);

        return FamilyDietData.builder()
                .diseases(diseases)
                .healthConditions(healthConditions)
                .preferredIngredients(preferences.preferredIngredients())
                .allergyIngredientIds(preferences.allergyIngredientIds())
                .fallbackExcludedIngredientIds(preferences.fallbackExcludedIngredientIds())
                .preferredIngredientNames(preferences.preferredIngredientNames())
                .dislikedIngredientNames(preferences.dislikedIngredientNames())
                .allergyIngredientNames(preferences.allergyIngredientNames())
                .build();
    }

    /**
     * 선호, 비선호 재료 정보
     *
     * @param users 가족 멤버
     * @return 재료 id, 이름 리스트
     */
    private IngredientPreferenceSummary getIngredientPreferenceSummary(List<User> users) {
        List<UserIngredientPreference> familyPreferences = userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users);

        List<Ingredient> allergyIngredients = getIngredientsAllergy(familyPreferences);

        Set<Long> allergyIngredientIds = allergyIngredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        List<Ingredient> preferredIngredients = getIngredients(
                familyPreferences,
                FoodPreferenceType.PREFERRED,
                allergyIngredientIds
        );

        List<Ingredient> dislikedIngredients = getIngredients(
                familyPreferences,
                FoodPreferenceType.DISLIKED,
                allergyIngredientIds
        );

        Set<Long> dislikedIngredientIds = dislikedIngredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        Set<Long> fallbackExcludedIngredientIds = new HashSet<>();
        fallbackExcludedIngredientIds.addAll(allergyIngredientIds);
        fallbackExcludedIngredientIds.addAll(dislikedIngredientIds);

        return IngredientPreferenceSummary.builder()
                .preferredIngredients(preferredIngredients)
                .dislikedIngredients(dislikedIngredients)
                .allergyIngredients(allergyIngredients)
                .allergyIngredientIds(allergyIngredientIds)
                .fallbackExcludedIngredientIds(fallbackExcludedIngredientIds)
                .preferredIngredientNames(extractIngredientNames(preferredIngredients))
                .dislikedIngredientNames(extractIngredientNames(dislikedIngredients))
                .allergyIngredientNames(extractIngredientNames(allergyIngredients))
                .build();
    }


    /**
     * 재료 이름 추출
     *
     * @param ingredients 재료
     * @return 재료 이름 리스트
     */
    private List<String> extractIngredientNames(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(Ingredient::getName)
                .distinct()
                .toList();
    }

    /**
     * 알레르기 재료 제거
     *
     * @param familiesPreference   가족 선호 메뉴
     * @param type                 선호 타입
     * @param allergyIngredientIds 알레르기 재료 아이디
     * @return 재료 목록
     */
    private static @NonNull List<Ingredient> getIngredients(List<UserIngredientPreference> familiesPreference, FoodPreferenceType type, Set<Long> allergyIngredientIds) {
        return familiesPreference.stream()
                .filter(ingredientPreference -> ingredientPreference.getPreference() == type)
                .map(UserIngredientPreference::getIngredient)
                .filter(ingredient -> !allergyIngredientIds.contains(ingredient.getId()))
                .distinct()
                .toList();
    }

    /**
     * 알레르기 재료 목록
     *
     * @param familiesPreference 가족 선호 재료
     * @return 재료 목록
     */
    private static @NonNull List<Ingredient> getIngredientsAllergy(List<UserIngredientPreference> familiesPreference) {
        return familiesPreference.stream()
                .filter(ingredientPreference -> ingredientPreference.getPreference() == FoodPreferenceType.ALLERGY)
                .map(UserIngredientPreference::getIngredient)
                .distinct()
                .toList();
    }


    /**
     * 가족 건강 정보 가져오기
     *
     * @param users 가족 구성원
     * @return 가족 건강 정보
     */
    private @NonNull List<AiDietGenerateDto.HealthCondition> getFamilyHealthConditions(List<User> users) {
        // 가족들의 성별, 연령 정보 추합
        List<UserHealthProfile> familiesHealth = userHealthRepository.findAllByUserInFetchUser(users);

        // 성별, 연령, 키, 몸무게
        return familiesHealth.stream()
                .map(health -> {
                    LocalDate birthDate = health.getUser().getBirthDate();
                    int age = Period.between(birthDate, LocalDate.now()).getYears();
                    return AiDietGenerateDto.HealthCondition.builder()
                            .gender(health.getGender())
                            .age(age)
                            .weight(health.getWeight())
                            .height(health.getHeight())
                            .build();
                })
                .toList();
    }

    /**
     * 가족 질병 정보
     *
     * @param users 가족 구성원
     * @return 가족 질병 정보
     */
    private @NonNull List<AiDietGenerateDto.Disease> getFamilyDiseases(List<User> users) {
        // 가족들의 질병 정보 추합
        List<UserDisease> familiesDisease = userDiseaseRepository.findAllByUserIn(users);

        // 질병 이름과 상세 설명만 추합
        return familiesDisease.stream()
                .map(disease ->
                        AiDietGenerateDto.Disease.builder()
                                .diseaseName(String.valueOf(disease.getDetailName()))
                                .description(disease.getDescription())
                                .build()
                )
                .toList();
    }

    @Builder
    private record IngredientPreferenceSummary(
            List<Ingredient> preferredIngredients,
            List<Ingredient> dislikedIngredients,
            List<Ingredient> allergyIngredients,
            Set<Long> allergyIngredientIds,
            Set<Long> fallbackExcludedIngredientIds,
            List<String> preferredIngredientNames,
            List<String> dislikedIngredientNames,
            List<String> allergyIngredientNames
    ) {
    }
}
