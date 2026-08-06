package kongju.pickmeal.application.diet;

import java.util.*;
import java.time.Period;
import java.time.LocalDate;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
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
import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.user.UserHealthProfile;
import kongju.pickmeal.core.diet.type.DietMenuSource;
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


@Slf4j
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
            DietGenerationDto.GenerateRequest request,
            LocalDate startDate,
            LocalDate endDate,
            List<Long> userMenuPickIds
    ) {
        DietGeneration generation = dietGenerationRepository.findById(generationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        log.info("생성 정보 조회 시작: generationId={}", generationId);
        generation.processing();
        log.info("생성 정보 조회 완료: generationId={}", generationId);
        log.info("데이터 전처리 시작: generationId={}", generationId);
        // 전처리
        AiDietGenerateDto.Command command = prepareAiDietGeneration(userId, request, startDate, endDate, userMenuPickIds);
        log.info("GPT API 호출 시작: generationId={}", generationId);
        // ai호출
        AiDietGenerateDto.Result result = dietAiGenerator.generate(command);
        log.info("GPT API 응답 수신: generationId={}", generationId);
        // 검증
        validateResult(result, command);
        log.info("GPT 응답 파싱 시작: generationId={}", generationId);
        // 식단 생성하기
        List<AiDietGenerateDto.MealPlan> mealPlans = createMealPlans(result, command);
        log.info("식단 DB 저장 시작: generationId={}", generationId);
        // 저장
        saveAiDietResult(generation, mealPlans, command);
        // 상태변경
        generation.completed();
        log.info("AI 식단 생성 최종 완료: generationId={}", generationId);
    }

    /**
     * Ai넣기전 전처리
     *
     * @param userId  유저 아이디
     * @param request 신청 날짜와 끼니 개수
     * @return 질병정보, 건강정보, 메뉴, 선호 비선호 재료 등
     */
    private AiDietGenerateDto.Command prepareAiDietGeneration(
            Long userId, DietGenerationDto.GenerateRequest request,
            LocalDate startDate, LocalDate endDate, List<Long> userMenuPickIds) {
        User user = userReader.getById(userId);
        Family family = user.getFamily();

        log.info("1. 사용자 조회 시작");
        List<User> users = userRepository.findAllFamily(family);
        log.info("2. 질병 정보 조회 시작");
        List<AiDietGenerateDto.Disease> diseases = getFamilyDiseases(users);
        log.info("3. 건강 정보 조회 시작");
        List<AiDietGenerateDto.HealthCondition> healthConditions = getFamilyHealthConditions(users);
        log.info("4. 사용자 선택 메뉴 정보 조회 시작");
        UserMenuPickPreparation userMenuPickPreparation = getUserMenus(userMenuPickIds);
        log.info("5. 사용자 선호 재료 정보 조회 시작");
        IngredientPreferenceSummary preferenceSummary = getIngredientPreferenceSummary(users);
        log.info("6. 사용자 메뉴 후보 추출 시작");
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
                request.dailyMealCount(),
                menuCandidates,
                userMenuPickPreparation.userMenus,
                userMenuPickPreparation.userMenuPicks,
                healthConditions,
                diseases,
                preferenceSummary
        );
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
        Map<Long, List<MenuIngredient>> menuIngredientMap = getMenuIngredientMap(menusForIngredientFetch);

//        선호 후보: 선호 재료 기반 메뉴 + 알레르기 메뉴 제거
        List<AiDietGenerateDto.MenuCandidate> preferredCandidates = toMenuCandidates(preferredMenus, menuIngredientMap, allergyIngredientIds);

//        fallback 후보: 전체 메뉴 + 알레르기, 싫어하는 메뉴 제거
        List<AiDietGenerateDto.MenuCandidate> fallbackCandidates = toMenuCandidates(fallbackMenus, menuIngredientMap, fallbackExcludedIngredientIds);

//        dishType별 개수 제한 + 부족분 보충
        List<AiDietGenerateDto.MenuCandidate> menuCandidates = new ArrayList<>();

//        menuCandidates.addAll(selectByDishTypeWithFallback(
//                preferredCandidates,
//                fallbackCandidates,
//                DishType.MAIN_DISH,
//                limit.mainLimit()
//        ));

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

//        int mainLimit = Math.min(totalMealCount, 50);
        int soupLimit = Math.min(totalMealCount, 60);
        int sideDishLimit = Math.min(totalMealCount * 2, 120);

        return DishTypeCandidateLimit.builder()
                .totalMealCount(totalMealCount)
//                .mainLimit(mainLimit)
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
     * @param userMenuPickIds 가족 선택 메뉴 아이디
     * @return 유저 선택 메뉴 리스트
     */
    private UserMenuPickPreparation getUserMenus(List<Long> userMenuPickIds) {
        List<UserMenuPick> userMenuPicks = userMenuPickRepository.findAllByIdInFetchMenu(userMenuPickIds);

        // 중복 제거
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

        // 재료와 메뉴 연결
        Map<Long, List<MenuIngredient>> menuIngredientMap =
                getMenuIngredientMap(pickedMenus);

        // ai에게 넘겨줄 객체
        List<AiDietGenerateDto.UserMenu> userMenus = userMenuPicks.stream()
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
                .toList();

        return UserMenuPickPreparation.builder()
                .userMenus(userMenus)
                .userMenuPicks(userMenuPicks)
                .build();
    }

    @Builder
    private record UserMenuPickPreparation(
            List<AiDietGenerateDto.UserMenu> userMenus,
            List<UserMenuPick> userMenuPicks
    ) {
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
     *
     * @param result  생성 데이터
     * @param command 전처리 데이터
     */
    private void validateResult(AiDietGenerateDto.Result result, AiDietGenerateDto.Command command) {
        Map<Long, DishType> dishTypeMap = buildDishTypeMap(command);
//
//        validateRankedIds(
//                result.mainDishMenuIds(),
//                DishType.MAIN_DISH,
//                dishTypeMap
//        );

        validateRankedIds(
                result.soupMenuIds(),
                DishType.SOUP,
                dishTypeMap
        );

        validateRankedIds(
                result.sideDishMenuIds(),
                DishType.SIDE_DISH,
                dishTypeMap
        );
    }

    private void validateRankedIds(
            List<Long> menuIds,
            DishType expectedDishType,
            Map<Long, DishType> dishTypeMap
    ) {
        if (menuIds == null || menuIds.isEmpty()) {
            log.error("AI 추천 메뉴 목록이 비어 있음: dishType={}", expectedDishType);
            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        Set<Long> uniqueMenuIds = new HashSet<>();

        for (Long menuId : menuIds) {
            if (menuId == null) {
                log.error("AI 추천 menuId가 null임: dishType={}", expectedDishType);
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            if (!uniqueMenuIds.add(menuId)) {
                log.error("AI 추천 menuId 중복: dishType={}, menuId={}", expectedDishType, menuId
                );
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            DishType actualDishType = dishTypeMap.get(menuId);

            if (actualDishType == null) {
                log.error("AI가 후보에 없는 menuId를 반환함: expectedDishType={}, menuId={}", expectedDishType, menuId);
                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }

            if (actualDishType != expectedDishType) {
                log.error("AI 추천 메뉴 타입 불일치: menuId={}, expected={}, actual={}", menuId, expectedDishType, actualDishType
                );
                throw new BusinessException(ErrorCode.INVALID_MENU_DATA);
            }
        }
    }

    /**
     * 메뉴 후보에서 id, dishType추출
     *
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

        for (AiDietGenerateDto.UserMenu userMenu : command.userMenus()) {
            dishTypeMap.putIfAbsent(userMenu.menuId(), userMenu.dishType());
        }

        return dishTypeMap;
    }

    /**
     * 식단 날짜에 넣기
     * @param result ai 식단 재배치
     * @param command 전처리 데이터
     * @return 식단 계획표
     */
    List<AiDietGenerateDto.MealPlan> createMealPlans(AiDietGenerateDto.Result result, AiDietGenerateDto.Command command) {
        List<AiDietGenerateDto.MealPlan> mealPlans = new ArrayList<>();

//        Iterator<Long> mainDishIterator = result.mainDishMenuIds().iterator();
        Iterator<Long> soupIterator = result.soupMenuIds().iterator();
        Iterator<Long> sideDishIterator = result.sideDishMenuIds().iterator();
        List<MealType> mealTypes = determineMealTypes(command.dailyMealCount());

        for (LocalDate date = command.startDate(); !date.isAfter(command.endDate()); date = date.plusDays(1)) {
            for (MealType mealType : mealTypes) {
                Long soupMenuId = getNextMenuId(soupIterator, DishType.SOUP, date, mealType);
                Long firstSideDishMenuId = getNextMenuId(sideDishIterator, DishType.SIDE_DISH, date, mealType);
                Long secondSideDishMenuId = getNextMenuId(sideDishIterator, DishType.SIDE_DISH, date, mealType);

                mealPlans.add(AiDietGenerateDto.MealPlan.builder()
                        .date(date).mealType(mealType).menuId(soupMenuId)
                        .build());

                mealPlans.add(AiDietGenerateDto.MealPlan.builder()
                        .date(date).mealType(mealType).menuId(firstSideDishMenuId)
                        .build());

                mealPlans.add(AiDietGenerateDto.MealPlan.builder()
                        .date(date).mealType(mealType).menuId(secondSideDishMenuId)
                        .build());
            }
        }

        return mealPlans;

    }

    /**
     * 식단 타입
     * @param dailyMealCount 하루 끼니
     * @return 아,점,저 선택
     */
    private List<MealType> determineMealTypes(int dailyMealCount) {
        return switch (dailyMealCount) {
            case 1 -> List.of(MealType.DINNER);
            case 2 -> List.of(MealType.LUNCH, MealType.DINNER);
            case 3 -> List.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);

            default -> {
                log.error("지원하지 않는 하루 식사 횟수: dailyMealCount={}", dailyMealCount);

                throw new BusinessException(ErrorCode.AI_DATA_ERROR);
            }
        };
    }

    /**
     * 메뉴 아이디 꺼내기
     * @param iterator 메뉴 배열
     * @param dishType 메인, 사이드, 국 타입
     * @param date 날짜
     * @param mealType 아,점,저
     * @return 다음 메뉴 반환
     */
    private Long getNextMenuId(Iterator<Long> iterator, DishType dishType, LocalDate date, MealType mealType) {
        if (!iterator.hasNext()) {
            log.error("AI 추천 메뉴 수 부족: dishType={}, date={}, mealType={}", dishType, date, mealType);
            throw new BusinessException(ErrorCode.AI_DATA_ERROR);
        }

        return iterator.next();
    }

    /**
     * DB에 데이터 저장
     *
     * @param generation DietGeneration
     * @param mealPlans  ai 생성 결과
     * @param command    전처리 데이터
     */
    void saveAiDietResult(DietGeneration generation, List<AiDietGenerateDto.MealPlan> mealPlans,
                          AiDietGenerateDto.Command command) {
        User user = userReader.getById(command.userId());
        Family family = user.getFamily();

        Set<Long> userPickMenuIds = command.userMenus().stream()
                .map(AiDietGenerateDto.UserMenu::menuId)
                .collect(Collectors.toSet());

        List<Diet> diets = mealPlans.stream()
                .map(mealPlan -> {
                    Menu menu = menuRepository.findById(mealPlan.menuId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
                    DietMenuSource source = userPickMenuIds.contains(mealPlan.menuId())
                            ? DietMenuSource.USER_PICKED
                            : DietMenuSource.AI_RECOMMENDED;

                    return Diet.create(family, menu, mealPlan.date(), mealPlan.mealType(), generation, source);
                })
                .toList();

        dietRepository.saveAll(diets);
        // 사용한 식단만 제거 - 아직까진 별 쓸모 없음 한번만 생성 가능하기 때문
        markUsedUserMenuPicks(mealPlans, command);
    }

    /**
     * 식단 생성에 사용된 메뉴 사용 처리
     *
     * @param mealPlans 생성 식단
     * @param command   전처리 데이터
     */
    private void markUsedUserMenuPicks(
            List<AiDietGenerateDto.MealPlan> mealPlans,
            AiDietGenerateDto.Command command
    ) {
        Set<Long> assignedMenuIds = mealPlans.stream()
                .map(AiDietGenerateDto.MealPlan::menuId)
                .collect(Collectors.toSet());

        Set<Long> usedUserMenuPickIds = command.userMenus().stream()
                .filter(userMenu ->
                        assignedMenuIds.contains(userMenu.menuId())
                )
                .map(AiDietGenerateDto.UserMenu::userMenuPickId)
                .collect(Collectors.toSet());

        command.userMenuPicks().stream()
                .filter(userMenuPick ->
                        usedUserMenuPickIds.contains(userMenuPick.getId())
                )
                .forEach(UserMenuPick::used);
    }

    /**
     * 빌더 패턴을 사용하여 생성
     *
     * @param userId            유저 id
     * @param startDate         시작 날짜
     * @param endDate           마지막 날짜
     * @param dailyMealCount    하루 식단 개수
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
            int dailyMealCount,
            List<AiDietGenerateDto.MenuCandidate> menuCandidates,
            List<AiDietGenerateDto.UserMenu> userMenus,
            List<UserMenuPick> userMenuPicks,
            List<AiDietGenerateDto.HealthCondition> healthConditions,
            List<AiDietGenerateDto.Disease> diseases,
            IngredientPreferenceSummary preferenceSummary
    ) {
        return AiDietGenerateDto.Command.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .dailyMealCount(dailyMealCount)
                .menuCandidates(menuCandidates)
                .userMenus(userMenus)
                .userMenuPicks(userMenuPicks)
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
//            int mainLimit,
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
