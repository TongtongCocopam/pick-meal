package kongju.pickmeal.application.user;

import java.util.List;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.LongStream;

import kongju.pickmeal.core.diet.Ingredient;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.user.type.DiseaseName;
import kongju.pickmeal.application.user.data.UserDto;
import kongju.pickmeal.core.diet.IngredientRepository;
import kongju.pickmeal.core.user.type.DiseaseCategory;
import kongju.pickmeal.core.user.UserDiseaseRepository;
import kongju.pickmeal.core.user.type.FoodPreferenceType;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.application.user.data.UserDietProfileDto;
import kongju.pickmeal.core.user.UserIngredientPreferenceRepository;

import static kongju.pickmeal.support.fixture.UserFixture.user;
import static kongju.pickmeal.fixture.MemberFixture.createRequest;


@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDiseaseRepository userDiseaseRepository;

    @Mock
    private UserIngredientPreferenceRepository userIngredientPreferenceRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("회원가입 테스트")
    class Signup {
        @Test
        @DisplayName("회원가입 시 중복된 아이디가 있으면 BusinessException을 던진다")
        public void should_fail_signup_when_id_is_duplicate() {
            UserDto.SignupRequest request = createRequest();
            // id 중복 확인 true로 함
            given(userRepository.existsByLoginId(any())).willReturn(true);

            // 회원가입 로직 실행
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);
        }

        @Test
        @DisplayName("회원가입 시 중복된 이메일이 있으면 BusinessException을 던진다")
        public void should_fail_signup_when_email_is_duplicate() {
            UserDto.SignupRequest request = createRequest();

            // email 중복 확인 true로 함
            given(userRepository.existsByEmail(any())).willReturn(true);
            // 회원가입 로직 실행
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);

        }

        @Test
        @DisplayName("비밀번호와 비밀번호 확인이 다르면 회원가입에 실패한다")
        void should_fail_when_password_mismatch() {
            // 비밀번호와 확인용 비밀번호를 다르게 설정
            UserDto.SignupRequest request = createRequest("test1234", "test0000!!", "wrong!!", "test@test.com", "test@test.com", "tester", LocalDate.now());

            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCH);
        }

        @Test
        @DisplayName("회원가입 성공")
        public void should_success_signup() {
            UserDto.SignupRequest request = createRequest();

            // 중복 확인 통과
            given(userRepository.existsByLoginId(any())).willReturn(false);
            given(userRepository.existsByEmail(any())).willReturn(false);


            // 비밀번호 암호화
            given(passwordEncoder.encode(anyString())).willReturn("hash_pw");

            User mockUser = user();
            given(userRepository.save(any(User.class))).willReturn(mockUser);

            UserDto.SignupResponse response = userService.signup(request);
            // 서비스 실행 후 user 체크
            // user 낚아오기
            ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(user.capture());

            // 가져온 객체 꺼내기
            User savedUser = user.getValue();
            assertThat(savedUser.getPassword()).isEqualTo("hash_pw");
            assertThat(response.nickName()).isEqualTo(request.nickName());
        }
    }

    @Nested
    @DisplayName("유저 건강, 선호 식품 정보 수정")
    class DietProfile {
        private UserDietProfileDto.IngredientPreferenceRequest createPreference(Long id) {
            return UserDietProfileDto.IngredientPreferenceRequest.builder()
                    .preference(FoodPreferenceType.PREFERRED)
                    .ingredientId(id)
                    .build();
        }

        private UserDietProfileDto.DiseaseRequest createDisease(DiseaseCategory category, DiseaseName diseaseName) {
            return UserDietProfileDto.DiseaseRequest.builder()
                    .category(category)
                    .detailName(diseaseName)
                    .build();
        }

        @Test
        @DisplayName("중복 재료 포함")
        public void should_fail_diet_profile_when_id_is_duplicate() {
            UserDietProfileDto.IngredientPreferenceRequest preference1 = createPreference(1L);
            UserDietProfileDto.IngredientPreferenceRequest preference2 = createPreference(1L);

            UserDietProfileDto.UpdateRequest request = UserDietProfileDto.UpdateRequest.builder()
                    .ingredientPreferences(List.of(preference1, preference2))
                    .build();
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateDietProfile(request, user()));

            assertEquals(ErrorCode.DUPLICATE_RESOURCE, exception.getErrorCode());
        }

        @Test
        @DisplayName("선호 재료 초과")
        public void should_fail_diet_profile_when_preference_is_duplicate() {
            List<UserDietProfileDto.IngredientPreferenceRequest> preferences = LongStream.range(1, 17)
                    .mapToObj(this::createPreference)
                    .toList();


            UserDietProfileDto.UpdateRequest request = UserDietProfileDto.UpdateRequest.builder()
                    .ingredientPreferences(preferences)
                    .diseases(List.of())
                    .build();

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateDietProfile(request, user()));

            assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
            assertEquals("선호 재료는 최대 15개까지 설정 가능합니다.", exception.getDetailMessage());
        }


        @Test
        @DisplayName("병명 중복")
        public void should_fail_diet_profile_when_disease_is_duplicate() {
            List<UserDietProfileDto.DiseaseRequest> diseases =
                    Collections.nCopies(2, createDisease(DiseaseCategory.DIGESTIVE, DiseaseName.GASTRITIS));

            UserDietProfileDto.UpdateRequest request = UserDietProfileDto.UpdateRequest.builder()
                    .ingredientPreferences(List.of())
                    .diseases(diseases)
                    .build();


            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateDietProfile(request, user()));

            assertEquals(ErrorCode.DUPLICATE_RESOURCE, exception.getErrorCode());
            assertEquals("중복되는 병명이 있습니다.", exception.getDetailMessage());
        }


        @Test
        @DisplayName("질병 분류와 일치하지 않는 병명")
        public void should_fail_diet_profile_when_not_correct_disease_name() {
            UserDietProfileDto.DiseaseRequest disease = createDisease(DiseaseCategory.IMMUNE, DiseaseName.GASTRITIS);

            UserDietProfileDto.UpdateRequest request = UserDietProfileDto.UpdateRequest.builder()
                    .ingredientPreferences(List.of())
                    .diseases(List.of(disease))
                    .build();


            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateDietProfile(request, user()));

            assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
            assertEquals("질병 분류와 상세 병명이 일치하지 않습니다.", exception.getDetailMessage());
        }


        @Test
        @DisplayName("없는 재료 ID")
        public void should_fail_diet_profile_when_not_exists_ingredient() {
            UserDietProfileDto.IngredientPreferenceRequest preference = createPreference(1L);
            UserDietProfileDto.UpdateRequest request = UserDietProfileDto.UpdateRequest.builder()
                    .ingredientPreferences(List.of(preference))
                    .diseases(List.of())
                    .build();

            given(ingredientRepository.findById(1L)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateDietProfile(request, user()));

            assertEquals(ErrorCode.INGREDIENT_NOT_FOUND, exception.getErrorCode());
            assertEquals("존재하지 않는 재료 아이디: [1]", exception.getDetailMessage());
        }


        @Test
        @DisplayName("성공케이스")
        public void should_success_diet_profile() {
            UserDietProfileDto.IngredientPreferenceRequest preference = createPreference(1L);
            UserDietProfileDto.DiseaseRequest disease = createDisease(DiseaseCategory.DIGESTIVE, DiseaseName.GASTRITIS);

            UserDietProfileDto.UpdateRequest request = UserDietProfileDto.UpdateRequest.builder()
                    .ingredientPreferences(List.of(preference))
                    .diseases(List.of(disease))
                    .build();

            Ingredient ingredient = Ingredient.builder()
                    .name("감자")
                    .build();

            given(ingredientRepository.findById(1L)).willReturn(Optional.ofNullable(ingredient));

            assertDoesNotThrow(() -> userService.updateDietProfile(request, user()));

            verify(userDiseaseRepository).saveAll(any());
            verify(userIngredientPreferenceRepository).saveAll(any());
        }

    }

}
