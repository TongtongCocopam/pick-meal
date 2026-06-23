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

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.user.UserDisease;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.DietAiGenerator;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.user.UserHealthProfile;
import kongju.pickmeal.core.diet.UserMenuPickRepository;
import kongju.pickmeal.core.user.type.FoodPreferenceType;
import kongju.pickmeal.core.user.UserIngredientPreference;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.user.repository.UserHealthRepository;
import kongju.pickmeal.core.user.repository.UserDiseaseRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;
import kongju.pickmeal.core.user.repository.UserIngredientPreferenceRepository;


@Service
@RequiredArgsConstructor
public class AiDietService {
    private final UserReader userReader;
    private final MenuRepository menuRepository;
    private final DietAiGenerator dietAiGenerator;

    private final UserRepository userRepository;
    private final UserHealthRepository userHealthRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final UserDiseaseRepository userDiseaseRepository;
    private final UserMenuPickRepository userMenuPickRepository;
    private final UserIngredientPreferenceRepository userIngredientPreferenceRepository;

    public DietGenerationDto.GenerateResponse generate(
            Long userId,
            DietGenerationDto.GenerateRequest request
    ) {
        User user = userReader.getById(userId);
        Family family = user.getFamily();

        LocalDate startDate = request.startDate();
        LocalDate endDate = calculateEndDate(startDate);

        List<User> users = userRepository.findAllFamily(family);

        List<AiDietGenerateDto.Disease> diseases = getFamilyDiseases(users);
        List<AiDietGenerateDto.HealthCondition> healthConditions = getFamilyHealthConditions(users);
        List<AiDietGenerateDto.UserMenu> userMenus = getUserMenus(users);

        IngredientPreferenceSummary preferenceSummary =
                getIngredientPreferenceSummary(users);

        List<AiDietGenerateDto.MenuCandidate> menuCandidates = getMenuCandidates(
                request.dailyMealCount(),
                startDate,
                endDate,
                preferenceSummary.preferredIngredients(),
                preferenceSummary.allergyIngredientIds(),
                preferenceSummary.fallbackExcludedIngredientIds()
        );

        AiDietGenerateDto.Command command = buildCommand(
                userId,
                startDate,
                endDate,
                menuCandidates,
                userMenus,
                healthConditions,
                diseases,
                preferenceSummary
        );

        // AiDietGenerateDto.Result result = dietAiGenerator.generate(command);
        // 검증
        // return DietGenerationDto.GenerateResponse.from(result);

        return null;
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
