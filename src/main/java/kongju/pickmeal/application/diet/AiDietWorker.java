package kongju.pickmeal.application.diet;

import java.util.*;
import java.time.Period;
import java.time.LocalDate;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.user.UserDisease;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.ai.DietAiGenerator;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.user.UserHealthProfile;
import kongju.pickmeal.core.user.type.FoodPreferenceType;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.user.UserIngredientPreference;
import kongju.pickmeal.core.diet.repository.DietRepository;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.core.user.repository.UserHealthRepository;
import kongju.pickmeal.core.user.repository.UserDiseaseRepository;
import kongju.pickmeal.core.diet.repository.UserMenuPickRepository;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;
import kongju.pickmeal.core.user.repository.UserIngredientPreferenceRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class AiDietWorker {
    private final UserReader userReader;
    private final MenuRepository menuRepository;

    private final UserRepository userRepository;
    private final DietRepository dietRepository;
    private final UserHealthRepository userHealthRepository;
    private final UserDiseaseRepository userDiseaseRepository;
    private final UserMenuPickRepository userMenuPickRepository;
    private final DietGenerationRepository dietGenerationRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final UserIngredientPreferenceRepository userIngredientPreferenceRepository;

    private final DietAiGenerator dietAiGenerator;

    /**
     * 식단 생성 데이터 전처리, 생성, 반환
     *
     * @param userId       유저 아이디
     * @param generationId 생성 아이디
     * @param request      요청 데이터
     */
    public void generate(
            Long userId,
            UUID generationId,
            DietGenerationDto.GenerateRequest request
    ) {
        DietGeneration generation = dietGenerationRepository.findById(generationId)
                .orElseThrow();

        generation.processing();
        // 전처리
        AiDietGenerateDto.Command command = prepareAiDietGeneration(userId, request);
        // ai호출
        AiDietGenerateDto.Result result = dietAiGenerator.generate(command);
        // 검증
        validateAiDietResult(result, command);
        // 저장
         saveAiDietResult(generation, result, command);
        // 상태변경
        generation.completed();
    }

    /**
     * Ai넣기전 전처리
     *
     * @param userId  유저 아이디
     * @param request 신청 날짜와 끼니 개수
     * @return 질병정보, 건강정보, 메뉴, 선호 비선호 재료 등
     */
    private AiDietGenerateDto.Command prepareAiDietGeneration(Long userId, DietGenerationDto.GenerateRequest request) {
        User user = userReader.getById(userId);
        Family family = user.getFamily();

        LocalDate startDate = request.startDate();
        LocalDate endDate = calculateEndDate(startDate);

        List<User> users = userRepository.findAllFamily(family);

        List<AiDietGenerateDto.Disease> diseases = getFamilyDiseases(users);
        List<AiDietGenerateDto.HealthCondition> healthConditions = getFamilyHealthConditions(users);
        List<AiDietGenerateDto.UserMenu> userMenus = getUserMenus(users);

        IngredientPreferenceSummary preferenceSummary = getIngredientPreferenceSummary(users);

        List<AiDietGenerateDto.MenuCandidate> menuCandidates = getMenuCandidates(
                request.dailyMealCount(),
                startDate,
                endDate,
                preferenceSummary.preferredIngredients(),
                preferenceSummary.allergyIngredientIds(),
                preferenceSummary.fallbackExcludedIngredientIds()
        );

        return buildCommand(
                userId,
                startDate,
                endDate,
                menuCandidates,
                userMenus,
                healthConditions,
                diseases,
                preferenceSummary
        );
    }

    /**
     * 마지막 식단 날짜 계산
     *
     * @param startDate 시작 날짜
     * @return 날짜
     */
    private LocalDate calculateEndDate(LocalDate startDate) {
        return startDate.withDayOfMonth(startDate.lengthOfMonth());
    }

    /**
     * 선호, 비선호 재료 정보
     *
     * @param users 가족 멤버
     * @return 재료 id, 이름 리스트
     */
    private IngredientPreferenceSummary getIngredientPreferenceSummary(List<User> users) {
        List<UserIngredientPreference> familyPreferences =
                userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users);

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
     * 메뉴 후보 추출
     *
     * @param dailyMealCount       하루 끼니
     * @param startDate            시작 날짜
     * @param endDate              끝
     * @param preferIngredients    선호 재료
     * @param allergyIngredientIds 알레르기 재료
     * @return 메뉴 후보 리스트
     */
    private @NonNull List<AiDietGenerateDto.MenuCandidate> getMenuCandidates(
            int dailyMealCount,
            LocalDate startDate,
            LocalDate endDate,
            List<Ingredient> preferIngredients,
            Set<Long> allergyIngredientIds,
            Set<Long> fallbackExcludedIngredientIds
    ) {
        DishTypeCandidateLimit limit = calculateCandidateLimit(startDate, endDate, dailyMealCount);
        // 선호메뉴에서 알레르기 메뉴 제거

        //선호 재료가 들어간 메뉴
        List<Menu> preferredMenus = findMenusByIngredients(preferIngredients);

        // 전체 메뉴 보수용
        List<Menu> fallbackMenus = menuRepository.findAll();

//        재료 조회 대상 메뉴 합치기
        List<Menu> menusForIngredientFetch = mergeMenus(preferredMenus, fallbackMenus);

//        메뉴별 재료 Map
        Map<Long, List<MenuIngredient>> menuIngredientMap =
                getMenuIngredientMap(menusForIngredientFetch);

//        선호 후보: 선호 재료 기반 메뉴 + 알레르기 메뉴 제거
        List<AiDietGenerateDto.MenuCandidate> preferredCandidates =
                toMenuCandidates(preferredMenus, menuIngredientMap, allergyIngredientIds);

//        fallback 후보: 전체 메뉴 + 알레르기, 싫어하는 메뉴 제거
        List<AiDietGenerateDto.MenuCandidate> fallbackCandidates =
                toMenuCandidates(fallbackMenus, menuIngredientMap, fallbackExcludedIngredientIds);

//        dishType별 개수 제한 + 부족분 보충
        List<AiDietGenerateDto.MenuCandidate> menuCandidates = new ArrayList<>();

        menuCandidates.addAll(selectByDishTypeWithFallback(
                preferredCandidates,
                fallbackCandidates,
                DishType.MAIN_DISH,
                limit.mainLimit()
        ));

        menuCandidates.addAll(selectByDishTypeWithFallback(
                preferredCandidates,
                fallbackCandidates,
                DishType.SOUP,
                limit.soupLimit()
        ));

        menuCandidates.addAll(selectByDishTypeWithFallback(
                preferredCandidates,
                fallbackCandidates,
                DishType.SIDE_DISH,
                limit.sideDishLimit()
        ));
        return menuCandidates;
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
     * 선호 재료로 메뉴 찾기
     *
     * @param ingredients 재료 리스트
     * @return 메뉴
     */
    private List<Menu> findMenusByIngredients(List<Ingredient> ingredients) {
        return ingredients.stream()
                .flatMap(ingredient -> menuIngredientRepository.findAllByIngredientWithMenu(ingredient).stream())
                .map(MenuIngredient::getMenu)
                .collect(Collectors.toMap(
                        Menu::getId,
                        menu -> menu,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .toList();
    }

    /**
     * 메뉴 병합
     *
     * @param preferredMenus 선호 메뉴
     * @param fallbackMenus  추가 메뉴
     * @return 메뉴 리스트
     */
    private List<Menu> mergeMenus(List<Menu> preferredMenus, List<Menu> fallbackMenus) {
        return Stream.concat(preferredMenus.stream(), fallbackMenus.stream())
                .collect(Collectors.toMap(
                        Menu::getId,
                        menu -> menu,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .toList();
    }

    /**
     * 메뉴 재료 맵
     *
     * @param menus 메뉴 리스트
     * @return 메뉴 아이디와 메뉴 재료
     */
    private Map<Long, List<MenuIngredient>> getMenuIngredientMap(List<Menu> menus) {
        if (menus.isEmpty()) {
            return Map.of();
        }

        List<MenuIngredient> menuIngredients =
                menuIngredientRepository.findAllByMenuInFetchIngredient(menus);

        return menuIngredients.stream()
                .collect(Collectors.groupingBy(menuIngredient -> menuIngredient.getMenu().getId()));
    }

    /**
     * 메뉴 후보에 추가
     *
     * @param menus                 메뉴
     * @param menuIngredientMap     아이디와 메뉴 재료 연결 테이블
     * @param excludedIngredientIds 알레르기 or 싫어하는 재료 아이디
     * @return 메뉴 후보 리스트
     */
    private List<AiDietGenerateDto.MenuCandidate> toMenuCandidates(
            List<Menu> menus,
            Map<Long, List<MenuIngredient>> menuIngredientMap,
            Set<Long> excludedIngredientIds
    ) {
        return menus.stream()
                .filter(menu -> menuIngredientMap.getOrDefault(menu.getId(), List.of()).stream()
                        .noneMatch(menuIngredient ->
                                excludedIngredientIds.contains(menuIngredient.getIngredient().getId())
                        )
                )
                .map(menu -> {
                    List<MenuIngredient> menuIngredients =
                            menuIngredientMap.getOrDefault(menu.getId(), List.of());

                    List<String> ingredientNames = menuIngredients.stream()
                            .map(menuIngredient -> menuIngredient.getIngredient().getName())
                            .distinct()
                            .toList();

                    return AiDietGenerateDto.MenuCandidate.builder()
                            .menuId(menu.getId())
                            .menuName(menu.getMenuName())
                            .dishType(menu.getDishType())
                            .ingredients(ingredientNames)
                            .build();
                })
                .toList();
    }

    /**
     * 추가 메뉴 선정
     *
     * @param preferredCandidates 선호 메뉴 후보
     * @param fallbackCandidates  추가 메뉴 후보
     * @param dishType            디쉬 타입
     * @param limit               제한
     * @return 추가된 메뉴
     */
    private List<AiDietGenerateDto.MenuCandidate> selectByDishTypeWithFallback(
            List<AiDietGenerateDto.MenuCandidate> preferredCandidates,
            List<AiDietGenerateDto.MenuCandidate> fallbackCandidates,
            DishType dishType,
            int limit
    ) {
        List<AiDietGenerateDto.MenuCandidate> selected = preferredCandidates.stream()
                .filter(candidate -> candidate.dishType() == dishType)
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(selected);

        selected = selected.stream()
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));

        if (selected.size() >= limit) {
            return selected;
        }

        Set<Long> selectedMenuIds = selected.stream()
                .map(AiDietGenerateDto.MenuCandidate::menuId)
                .collect(Collectors.toSet());

        int lackCount = limit - selected.size();

        List<AiDietGenerateDto.MenuCandidate> fallback = fallbackCandidates.stream()
                .filter(candidate -> candidate.dishType() == dishType)
                .filter(candidate -> !selectedMenuIds.contains(candidate.menuId()))
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(fallback);

        selected.addAll(
                fallback.stream()
                        .limit(lackCount)
                        .toList()
        );

        return selected;
    }

    /**
     * 기간에 따른 식단 후보 개수 조정
     *
     * @param startDate      시작
     * @param endDate        끝
     * @param dailyMealCount 하루 식단 개수
     * @return 식단 후보 개수
     */
    private DishTypeCandidateLimit calculateCandidateLimit(
            LocalDate startDate,
            LocalDate endDate,
            Integer dailyMealCount
    ) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int totalMealCount = Math.toIntExact(days * dailyMealCount);

        int mainLimit = Math.min(totalMealCount, 50);
        int soupLimit = Math.clamp(totalMealCount / 2, 5, 30);
        int sideDishLimit = Math.min(totalMealCount * 3, 120);

        return DishTypeCandidateLimit.builder()
                .totalMealCount(totalMealCount)
                .mainLimit(mainLimit)
                .soupLimit(soupLimit)
                .sideDishLimit(sideDishLimit)
                .build();
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
     * 유저가 선택한 메뉴
     *
     * @param users 가족
     * @return 유저 선택 메뉴 리스트
     */
    private List<AiDietGenerateDto.UserMenu> getUserMenus(List<User> users) {
        List<UserMenuPick> userMenuPicks =
                userMenuPickRepository.findAllByUserInFetchMenu(users);

        List<Menu> pickedMenus = userMenuPicks.stream()
                .map(UserMenuPick::getMenu)
                .collect(Collectors.toMap(
                        Menu::getId,
                        menu -> menu,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .toList();

        Map<Long, List<MenuIngredient>> menuIngredientMap =
                getMenuIngredientMap(pickedMenus);

        return userMenuPicks.stream()
                .map(userMenuPick -> {
                    Menu menu = userMenuPick.getMenu();

                    List<String> ingredients = menuIngredientMap
                            .getOrDefault(menu.getId(), List.of())
                            .stream()
                            .map(menuIngredient -> menuIngredient.getIngredient().getName())
                            .distinct()
                            .toList();

                    return AiDietGenerateDto.UserMenu.builder()
                            .userMenuPickId(userMenuPick.getId())
                            .menuId(menu.getId())
                            .menuName(menu.getMenuName())
                            .dishType(menu.getDishType())
                            .ingredients(ingredients)
                            .build();
                })
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

    /**
     * ai생성 데이터 검증
     * @param result 생성 데이터
     * @param command 전처리 데이터
     */
    private void validateAiDietResult(AiDietGenerateDto.Result result, AiDietGenerateDto.Command command) {
        // 날짜가 일치하는지
        if (!result.startDate().equals(command.startDate())
                || !result.endDate().equals(command.endDate())) {
            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }
        // mealPlans가 null인지
        if (result.mealPlans() == null || result.mealPlans().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        //menuId가 제공한 후보 목록 안에 있는지
        Map<Long, DishType> dishTypeMap = buildDishTypeMap(command);

        // 기본 필드 검사
        validateMealPlanBasicFields(result, command, dishTypeMap);
        // 날짜 다 차있는지 검사
        validateDateCoverage(result, command);

        Map<String, List<AiDietGenerateDto.MealPlan>> grouped =
                result.mealPlans().stream()
                        .collect(Collectors.groupingBy(
                                mealPlan -> mealPlan.date() + ":" + mealPlan.mealType()
                        ));

        for (List<AiDietGenerateDto.MealPlan> mealGroup : grouped.values()) {
            // 개수 확인
            validateMealGroup(mealGroup, dishTypeMap);
        }
    }

    /**
     * 메뉴 후보에서 id, dishType추출
     * @param command ai에 넣을 전처리한 데이터
     * @return map
     */
    private Map<Long, DishType> buildDishTypeMap(
            AiDietGenerateDto.Command command
    ) {
        Map<Long, DishType> dishTypeMap = new HashMap<>();

        for (AiDietGenerateDto.MenuCandidate candidate : command.menuCandidates()) {
            dishTypeMap.put(candidate.menuId(), candidate.dishType());
        }

        for (AiDietGenerateDto.UserMenu userMenu : command.userMenuPicks()) {
            dishTypeMap.putIfAbsent(userMenu.menuId(), userMenu.dishType());
        }

        return dishTypeMap;
    }

    /**
     * 기본 필드 검사
     * @param result 결과
     * @param command 전처리 데이터
     * @param dishTypeMap menuId, dishType
     */
    private void validateMealPlanBasicFields(
            AiDietGenerateDto.Result result,
            AiDietGenerateDto.Command command,
            Map<Long, DishType> dishTypeMap
    ) {
        for (AiDietGenerateDto.MealPlan mealPlan : result.mealPlans()) {
            //null체크
            if (mealPlan.date() == null
                    || mealPlan.mealType() == null
                    || mealPlan.menuId() == null) {
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            if (mealPlan.date().isBefore(command.startDate())
                    || mealPlan.date().isAfter(command.endDate())) {
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            // 포함되지 않은 메뉴 아이디
            if (!dishTypeMap.containsKey(mealPlan.menuId())) {
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            if (dishTypeMap.get(mealPlan.menuId()) == null) {
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }
        }
    }

    /**
     * 날짜 사이에 빈 데이터가 없는지 확인
     * @param result 결과
     * @param command 요청 날짜
     */
    private void validateDateCoverage(
            AiDietGenerateDto.Result result,
            AiDietGenerateDto.Command command
    ) {
        Set<LocalDate> resultDates = result.mealPlans().stream()
                .map(AiDietGenerateDto.MealPlan::date)
                .collect(Collectors.toSet());

        LocalDate current = command.startDate();

        while (!current.isAfter(command.endDate())) {
            if (!resultDates.contains(current)) {
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            current = current.plusDays(1);
        }
    }

    /**
     * 한끼에 dishType수가 맞는지와 중복 체크
     * @param mealGroup 한끼 반찬, 메뉴
     * @param dishTypeMap menuId, dishType
     */
    private void validateMealGroup(
            List<AiDietGenerateDto.MealPlan> mealGroup,
            Map<Long, DishType> dishTypeMap
    ) {
        Set<Long> menuIdsInMeal = new HashSet<>();

        int mainCount = 0;
        int soupCount = 0;
        int sideDishCount = 0;

        for (AiDietGenerateDto.MealPlan mealPlan : mealGroup) {
            Long menuId = mealPlan.menuId();

            if (!menuIdsInMeal.add(menuId)) {
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            DishType dishType = dishTypeMap.get(menuId);

            if (dishType == DishType.MAIN_DISH) {
                mainCount++;
            } else if (dishType == DishType.SOUP) {
                soupCount++;
            } else if (dishType == DishType.SIDE_DISH) {
                sideDishCount++;
            }
        }

        validateDishCombination(mainCount, soupCount, sideDishCount);
    }

    /**
     * 개수 확인
     * @param mainCount 메인 메뉴
     * @param soupCount 국
     * @param sideDishCount 반찬
     */
    private void validateDishCombination(
            int mainCount,
            int soupCount,
            int sideDishCount
    ) {
        if (mainCount == 0 && soupCount == 0 && sideDishCount > 0) {
            throw new BusinessException(ErrorCode.INVALID_MENU_DATA);
        }

        if (soupCount > 0 && sideDishCount < 2) {
            throw new BusinessException(ErrorCode.INVALID_MENU_DATA);
        }
    }

    /**
     * DB에 데이터 저장
     * @param generation DietGeneration
     * @param result ai 생성 결과
     * @param command 전처리 데이터
     */
    void saveAiDietResult(DietGeneration generation, AiDietGenerateDto.Result result, AiDietGenerateDto.Command command){
        User user = userReader.getById(command.userId());
        Family family = user.getFamily();

        List<Diet> diets = result.mealPlans().stream()
                .map(mealPlan -> {
                    Menu menu = menuRepository.findById(mealPlan.menuId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
                    return Diet.create(family, menu, mealPlan.date(), mealPlan.mealType(), generation);
                })
                .toList();

        dietRepository.saveAll(diets);
    }

    /**
     * 빌더 패턴을 사용하여 생성
     *
     * @param userId            유저 id
     * @param startDate         시작 날짜
     * @param endDate           마지막 날짜
     * @param menuCandidates    메뉴 후보
     * @param userMenus         선택한 메뉴 후보
     * @param healthConditions  건강 정보
     * @param diseases          질병 정보
     * @param preferenceSummary 선호 정보
     * @return ai넣을 데이터
     */
    private AiDietGenerateDto.Command buildCommand(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            List<AiDietGenerateDto.MenuCandidate> menuCandidates,
            List<AiDietGenerateDto.UserMenu> userMenus,
            List<AiDietGenerateDto.HealthCondition> healthConditions,
            List<AiDietGenerateDto.Disease> diseases,
            IngredientPreferenceSummary preferenceSummary
    ) {
        return AiDietGenerateDto.Command.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .menuCandidates(menuCandidates)
                .userMenuPicks(userMenus)
                .healthConditions(healthConditions)
                .disease(diseases)
                .preferredIngredients(preferenceSummary.preferredIngredientNames())
                .dislikedIngredients(preferenceSummary.dislikedIngredientNames())
                .allergyIngredients(preferenceSummary.allergyIngredientNames())
                .build();
    }

    @Builder
    private record DishTypeCandidateLimit(
            int totalMealCount,
            int mainLimit,
            int soupLimit,
            int sideDishLimit
    ) {
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
