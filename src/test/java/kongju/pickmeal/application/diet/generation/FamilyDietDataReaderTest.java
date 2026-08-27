package kongju.pickmeal.application.diet.generation;

import java.util.List;
import java.time.LocalDate;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.user.UserDisease;
import kongju.pickmeal.core.user.UserHealthProfile;
import kongju.pickmeal.core.user.type.FoodPreferenceType;
import kongju.pickmeal.core.user.UserIngredientPreference;
import kongju.pickmeal.application.diet.data.FamilyDietDataDto;
import kongju.pickmeal.core.user.repository.UserHealthRepository;
import kongju.pickmeal.core.user.repository.UserDiseaseRepository;
import kongju.pickmeal.core.user.repository.UserIngredientPreferenceRepository;


@ExtendWith(MockitoExtension.class)
public class FamilyDietDataReaderTest {
    @Mock
    private UserHealthRepository userHealthRepository;
    @Mock
    private UserDiseaseRepository userDiseaseRepository;
    @Mock
    private UserIngredientPreferenceRepository userIngredientPreferenceRepository;
    @InjectMocks
    private FamilyDietDataReader reader;

    @Test
    @DisplayName("가족의 선호, 비선호, 알레르기 재료를 분류")
    void should_classify_family_ingredient_preferences() {
        User user = mock(User.class);
        List<User> users = List.of(user);

        Ingredient preferredIngredient = ingredient(1L, "두부");
        Ingredient dislikedIngredient = ingredient(2L, "가지");
        Ingredient allergyIngredient = ingredient(3L, "땅콩");

        UserIngredientPreference preferred = preference(preferredIngredient, FoodPreferenceType.PREFERRED);
        UserIngredientPreference disliked = preference(dislikedIngredient, FoodPreferenceType.DISLIKED);
        UserIngredientPreference allergy = preference(allergyIngredient, FoodPreferenceType.ALLERGY);

        when(userDiseaseRepository.findAllByUserIn(users)).thenReturn(List.of());

        when(userHealthRepository.findAllByUserInFetchUser(users)).thenReturn(List.of());
        when(userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users))
                .thenReturn(List.of(
                        preferred,
                        disliked,
                        allergy
                ));

        FamilyDietDataDto result = reader.read(users);
        assertThat(result.preferredIngredients()).containsExactly(preferredIngredient);
        assertThat(result.preferredIngredientNames()).containsExactly("두부");
        assertThat(result.dislikedIngredientNames()).containsExactly("가지");
        assertThat(result.allergyIngredientNames()).containsExactly("땅콩");
        assertThat(result.allergyIngredientIds()).containsExactly(3L);
        assertThat(result.fallbackExcludedIngredientIds()).containsExactlyInAnyOrder(2L, 3L);
    }


    @Test
    @DisplayName("알레르기 재료는 선호 및 비선호 재료에서 제외")
    void should_exclude_allergy_ingredient_from_preferences() {
        User user = mock(User.class);
        List<User> users = List.of(user);

        Ingredient peanut = ingredient(1L, "땅콩");
        Ingredient tofu = ingredient(2L, "두부");

        UserIngredientPreference preferredPeanut = preference(peanut, FoodPreferenceType.PREFERRED);
        UserIngredientPreference dislikedPeanut = preference(peanut, FoodPreferenceType.DISLIKED);
        UserIngredientPreference allergyPeanut = preference(peanut, FoodPreferenceType.ALLERGY);
        UserIngredientPreference preferredTofu = preference(tofu, FoodPreferenceType.PREFERRED);

        when(userDiseaseRepository.findAllByUserIn(users)).thenReturn(List.of());
        when(userHealthRepository.findAllByUserInFetchUser(users)).thenReturn(List.of());
        when(userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users))
                .thenReturn(List.of(
                        preferredPeanut,
                        dislikedPeanut,
                        allergyPeanut,
                        preferredTofu
                ));

        FamilyDietDataDto result = reader.read(users);

        assertThat(result.preferredIngredientNames()).containsExactly("두부");
        assertThat(result.dislikedIngredientNames()).isEmpty();
        assertThat(result.allergyIngredientNames()).containsExactly("땅콩");
        assertThat(result.allergyIngredientIds()).containsExactly(1L);
        assertThat(result.fallbackExcludedIngredientIds()).containsExactly(1L);
    }


    @Test
    @DisplayName("가족 건강 정보를 AI 건강 정보로 변환")
    void should_convert_family_health_profile() {
        User user = mock(User.class);
        UserHealthProfile health = mock(UserHealthProfile.class);

        List<User> users = List.of(user);

        LocalDate birthDate = LocalDate.now()
                .minusYears(30)
                .minusDays(1);

        when(user.getBirthDate()).thenReturn(birthDate);

        when(health.getUser()).thenReturn(user);

        when(userDiseaseRepository.findAllByUserIn(users)).thenReturn(List.of());

        when(userHealthRepository.findAllByUserInFetchUser(users)).thenReturn(List.of(health));

        when(userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users))
                .thenReturn(List.of());

        FamilyDietDataDto result = reader.read(users);

        assertThat(result.healthConditions()).hasSize(1);

        assertThat(result.healthConditions().get(0).age())
                .isEqualTo(30);
    }


    @Test
    @DisplayName("가족 질병 정보를 AI 질병 정보로 변환")
    void should_convert_family_disease() {
        User user = mock(User.class);
        UserDisease userDisease = mock(UserDisease.class);

        List<User> users = List.of(user);

        when(userDisease.getDescription()).thenReturn("나트륨 섭취 관리 필요");
        when(userDiseaseRepository.findAllByUserIn(users)).thenReturn(List.of(userDisease));
        when(userHealthRepository.findAllByUserInFetchUser(users)).thenReturn(List.of());
        when(userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users))
                .thenReturn(List.of());

        FamilyDietDataDto result = reader.read(users);

        assertThat(result.diseases()).hasSize(1);
        assertThat(result.diseases().get(0).description())
                .isEqualTo("나트륨 섭취 관리 필요");
    }


    @Test
    @DisplayName("동일한 재료 이름은 AI 전달 목록에서 중복 제거")
    void should_remove_duplicate_ingredient_names() {
        User user = mock(User.class);
        List<User> users = List.of(user);

        Ingredient tofu1 = ingredient(1L, "두부");
        Ingredient tofu2 = ingredient(2L, "두부");

        UserIngredientPreference first = preference(tofu1, FoodPreferenceType.PREFERRED);
        UserIngredientPreference second = preference(tofu2, FoodPreferenceType.PREFERRED);

        when(userDiseaseRepository.findAllByUserIn(users)).thenReturn(List.of());
        when(userHealthRepository.findAllByUserInFetchUser(users)).thenReturn(List.of());
        when(userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users)).thenReturn(List.of(first, second));

        FamilyDietDataDto result = reader.read(users);
        assertThat(result.preferredIngredientNames())
                .containsExactly("두부");
    }


    private Ingredient ingredient(Long id, String name) {
        Ingredient ingredient = mock(Ingredient.class);
        when(ingredient.getId()).thenReturn(id);
        when(ingredient.getName()).thenReturn(name);

        return ingredient;
    }


    private UserIngredientPreference preference(
            Ingredient ingredient,
            FoodPreferenceType type
    ) {
        UserIngredientPreference preference = mock(UserIngredientPreference.class);
        when(preference.getIngredient()).thenReturn(ingredient);
        when(preference.getPreference()).thenReturn(type);

        return preference;
    }
}
