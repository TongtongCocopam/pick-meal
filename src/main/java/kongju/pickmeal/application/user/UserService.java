package kongju.pickmeal.application.user;

import java.util.*;
import java.time.LocalDate;
import java.util.regex.Pattern;

import kongju.pickmeal.core.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import kongju.pickmeal.core.user.*;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.application.user.data.*;
import kongju.pickmeal.core.user.type.DiseaseName;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.IngredientRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserReader userReader;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IngredientRepository ingredientRepository;
    private final UserDiseaseRepository userDiseaseRepository;
    private final UserHealthRepository userHealthRepository;
    private final UserIngredientPreferenceRepository userIngredientPreferenceRepository;
    private final UserPickCountRepository userPickCountRepository;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\W]{8,16}$");

    private static final int MAX_ALLERGY_INGREDIENT_COUNT = 30;
    private static final int MAX_PREFERRED_INGREDIENT_COUNT = 15;
    private static final int MAX_DISLIKED_INGREDIENT_COUNT = 15;

    /**
     * 회원가입 기능
     *
     * @param request 회원가입 정보
     * @return 닉네임 반환
     */
    public UserDto.SignupResponse signup(UserDto.SignupRequest request) {
        // 중복 이메일, 아이디 확인
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, request.loginId());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, request.email());
        }
        validateResisterRequest(request);
        // 비밀번호 해시 저장
        String password = passwordEncoder.encode(request.password());

        User user = User.create(request.nickname(), request.birthDate(), request.loginId(), request.email(), password);
        User savedUser = userRepository.save(user);

        UserPickCount userPickCount = UserPickCount.initialize(savedUser);
        userPickCountRepository.save(userPickCount);

        return UserDto.SignupResponse.builder()
                .userId(savedUser.getId())
                .nickname(savedUser.getNickname())
                .build();
    }

    /**
     * 회원가입 데이터 유효성 검사하고 에러 처리
     *
     * @param request 회원가입시 필요한 데이터
     */
    private void validateResisterRequest(UserDto.SignupRequest request) {
        // 아이디 길이 검사 (6~15자)
        if (request.loginId().length() < 6 || request.loginId().length() > 15) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "아이디는 6~15자 사이여야 합니다.");
        }

        // 비밀번호 일치 여부 검사
        if (!Objects.equals(request.password(), request.passwordCheck())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 비밀번호 정규식 검사 (영문, 숫자 포함 8~16자)
        if (!PASSWORD_PATTERN.matcher(request.password()).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호는 8~16자 사이여야 합니다.");
        }

        // 이메일 형식 검사 (간단한 정규식 또는 라이브러리 활용)
        if (!request.email().contains("@")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 생년월일 미래 날짜 여부 검사 (추가적인 비즈니스 로직)
        if (request.birthDate().isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 유저 건강 정보, 선호 식품 정보를 업데이트 하는 메서드
     *
     * @param request 질병 리스트, 기호 식품 리스트
     * @param userId  신청 유저 객체
     */
    public void updateDisease(UserDietProfileDto.UpdateDiseaseRequest request, Long userId) {
        User user = userReader.getById(userId);

        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "변경할 데이터가 존재하지 않습니다.");
        }

        // 질병 정보 유효한지 확인
        List<UserDietProfileDto.DiseaseRequest> diseases = request.diseases();
        validateDisease(diseases);

        userDiseaseRepository.deleteAllByUser(user);

        List<UserDisease> userDiseases = Objects.requireNonNull(diseases)
                .stream()
                .map(disease ->
                        UserDisease.create(disease.category(), disease.detailName(), disease.description(), user)
                )
                .toList();

        userDiseaseRepository.saveAll(userDiseases);

    }

    /**
     * 질병 유효 검증
     *
     * @param diseases 질병 목록
     */
    private void validateDisease(List<UserDietProfileDto.DiseaseRequest> diseases) {
        Set<DiseaseName> diseaseType = new HashSet<>();

        for (UserDietProfileDto.DiseaseRequest disease : diseases) {
            // 중복되는 질병이 있는지 체크
            if (!diseaseType.add(disease.detailName())) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "중복되는 병명이 있습니다.");
            }

            // 카테고리와 병명 종류가 일치하는지
            if (!disease.detailName().isInCategory(disease.category())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "질병 분류와 상세 병명이 일치하지 않습니다.");
            }

        }
    }

    /**
     * 재료 선호도 업데이트
     *
     * @param request 선호도 정보
     * @param userId  유저 아이디
     */
    public void updateIngredientPreference(UserDietProfileDto.UpdateIngredientPreferenceRequest request, Long userId) {
        User user = userReader.getById(userId);

        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "변경할 데이터가 존재하지 않습니다.");
        }

        // 알레르기 정보는 최대 30개 받을 수 있음
        // 선호 비선호 각 15개 제한
        List<UserDietProfileDto.IngredientPreferenceRequest> preferences = request.preferences();
        validateIngredientPreferences(preferences);

        userIngredientPreferenceRepository.deleteAllByUser(user);

        List<UserIngredientPreference> userIngredientPreferences = Objects.requireNonNull(preferences)
                .stream()
                .map(preference -> {
                    Ingredient ingredient = ingredientRepository.findById(preference.ingredientId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND, "존재하지 않는 재료 아이디: [" + preference.ingredientId() + "]"));

                    return UserIngredientPreference.builder()
                            .ingredient(ingredient)
                            .preference(preference.preference())
                            .user(user)
                            .build();
                })
                .toList();

        userIngredientPreferenceRepository.saveAll(userIngredientPreferences);

    }

    /**
     * 알레르기, 선호, 비선호 식품 개수 확인
     *
     * @param preferences 재료 id와 개수를 담은 객체
     */
    private void validateIngredientPreferences(
            List<UserDietProfileDto.IngredientPreferenceRequest> preferences
    ) {
        if (preferences == null || preferences.isEmpty()) {
            return;
        }

        int allergyCount = 0, preferredCount = 0, dislikedCount = 0;

        Set<Long> ingredientIds = new HashSet<>();

        for (UserDietProfileDto.IngredientPreferenceRequest preference : preferences) {
            // 선호하는 재료와 비선호 재료가 겹칠 경우
            if (!ingredientIds.add(preference.ingredientId())) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "중복된 재료가 포함되어 있습니다.");
            }

            switch (preference.preference()) {
                case ALLERGY -> allergyCount++;
                case PREFERRED -> preferredCount++;
                case DISLIKED -> dislikedCount++;
            }
        }

        if (allergyCount > MAX_ALLERGY_INGREDIENT_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "알레르기 재료는 최대 30개까지 설정 가능합니다.");
        }

        if (preferredCount > MAX_PREFERRED_INGREDIENT_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "선호 재료는 최대 15개까지 설정 가능합니다.");
        }

        if (dislikedCount > MAX_DISLIKED_INGREDIENT_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비선호 재료는 최대 15개까지 설정 가능합니다.");
        }
    }

    /**
     * 성별, 몸무게, 키 등 정보 수정
     *
     * @param request 유저 정보
     * @param userId  유저
     */
    public void updateHealth(UserHealthDto.UpdateRequest request, Long userId) {
        User user = userReader.getById(userId);

        UserHealthProfile health = userHealthRepository.findByUser(user)
                .orElseGet(() -> UserHealthProfile.create(null, null, null, user));

        health.update(
                request.gender(),
                request.height(),
                request.weight()
        );

        // 키, 몸무게, 성별 입력
        userHealthRepository.save(health);
    }

    /**
     * 닉네임, 생일 수정
     *
     * @param request 유저 정보
     * @param userId  유저
     * @return 수정된 결과
     */
    public UserProfileDto.UpdateResponse updateProfile(UserProfileDto.UpdateRequest request, Long userId) {
        User user = userReader.getById(userId);

        String nickname = request.nickname();
        LocalDate birthDate = request.birthDate();

        if (nickname == null && birthDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "변경할 데이터가 존재하지 않습니다.");
        }

        // null인 것만 빼고 적용
        if (nickname != null) {
            user.updateNickname(nickname);
        }

        if (birthDate != null) {
            user.updateBirthDate(birthDate);
        }

        return UserProfileDto.UpdateResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .email(user.getEmail())
                .loginId(user.getLoginId())
                .build();
    }

    /**
     * 비밀번호 변경
     *
     * @param request 변경할 비밀번호, 기존 비밀번호
     * @param userId  유저
     */
    public void updatePassword(UserPasswordDto.UpdateRequest request, Long userId) {
        User user = userReader.getById(userId);
        // 현재 비빌번호 일치 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호 확인과 일치하는지 확인하고 저장
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.MISMATCH_CONFIRM_PASSWORD);
        }

        // 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        String password = passwordEncoder.encode(request.newPassword());
        user.updatePassword(password);
    }
}
