package kongju.pickmeal.application.diet;

import java.util.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.user.UserPickCount;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.user.PickCountHistory;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.menu.type.IngredientUnit;
import kongju.pickmeal.application.diet.data.DietDto;
import kongju.pickmeal.core.diet.type.DietMenuSource;
import kongju.pickmeal.core.diet.type.UserMenuPickStatus;
import kongju.pickmeal.application.diet.data.DietMenuDto;
import kongju.pickmeal.core.user.type.FoodPreferenceType;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.core.user.UserIngredientPreference;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.diet.type.DietGenerationStatus;
import kongju.pickmeal.core.diet.repository.DietRepository;
import kongju.pickmeal.core.user.repository.UserRepository;
import kongju.pickmeal.core.family.repository.FamilyRepository;
import kongju.pickmeal.core.diet.repository.UserMenuPickRepository;
import kongju.pickmeal.core.user.repository.UserPickCountRepository;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;
import kongju.pickmeal.core.user.repository.PickCountHistoryRepository;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;
import kongju.pickmeal.application.diet.event.DietGenerationRequestedEvent;
import kongju.pickmeal.core.user.repository.UserIngredientPreferenceRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class DietService {
    private final UserReader userReader;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final DietRepository dietRepository;
    private final FamilyRepository familyRepository;
    private final UserMenuPickRepository userMenuPickRepository;
    private final UserPickCountRepository userPickCountRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final DietGenerationRepository dietGenerationRepository;
    private final PickCountHistoryRepository pickCountHistoryRepository;
    private final UserIngredientPreferenceRepository userIngredientPreferenceRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 메뉴 선택
     *
     * @param userId  유저 아이디
     * @param request 메뉴 선택 리스트
     * @return 메뉴 선택 정보
     */
    public MenuPickDto.CreateResponse menuPick(
            Long userId,
            MenuPickDto.CreateRequest request) {

        User user = userReader.getById(userId);
        List<Long> menuIds = request.menuIds().stream()
                .distinct()
                .toList();

        Long count = (long) menuIds.size();
        YearMonth targetMonth = request.targetMonth();
        validateSelectableMonth(targetMonth);
        debitPickCount(user, count);
        UUID uuid = UUID.randomUUID();
        debitHistory(user, count, uuid);

        // 유저가 선택한 메뉴들을 유저 픽 연결 테이블에 넣기
        List<UserMenuPick> userMenuPickList = menuIds.stream()
                .map(menuId -> {
                    Menu menu = getMenu(menuId);

                    return UserMenuPick.create(user, menu, targetMonth.atDay(1), uuid);
                })
                .toList();

        List<UserMenuPick> saveUserMenuPickList = userMenuPickRepository.saveAll(userMenuPickList);
        List<MenuPickDto.itemResponse> itemResponses = saveUserMenuPickList.stream()
                .map(userMenuPick ->
                        MenuPickDto.itemResponse.builder()
                                .pickId(userMenuPick.getId())
                                .menuId(userMenuPick.getMenu().getId())
                                .menuName(userMenuPick.getMenu().getMenuName())
                                .build()
                ).toList();

        return MenuPickDto.CreateResponse.builder()
                .pickedCount(saveUserMenuPickList.size())
                .items(itemResponses)
                .build();
    }

    /**
     * 식단 선택 허용 가능한 달인지
     *
     * @param targetMonth ai생성에 사용할 달
     */
    private void validateSelectableMonth(YearMonth targetMonth) {
        YearMonth now = YearMonth.now();

        if (!targetMonth.equals(now) && !targetMonth.equals(now.plusMonths(1))) {
            throw new BusinessException(ErrorCode.INVALID_TARGET_MONTH);
        }
    }

    /**
     * 선택권 차감
     *
     * @param user  사용 유저
     * @param count 개수
     */
    private void debitPickCount(User user, Long count) {
        UserPickCount userPickCount = userPickCountRepository.findByUserForUpdate(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 선택권 정보를 찾을 수 없습니다."));

        userPickCount.useCount(count);
    }

    /**
     * 선택권 사용 기록
     *
     * @param user          유저
     * @param count         개수
     * @param transactionId 사용 아이디
     */
    private void debitHistory(User user, Long count, UUID transactionId) {
        PickCountHistory pickCountHistory = PickCountHistory.debit(user, count, transactionId);
        pickCountHistoryRepository.save(pickCountHistory);
    }

    /**
     * 선택한 메뉴를 변경하는 기능
     *
     * @param userId  유저 아이디
     * @param request 변경할 메뉴
     * @return 메뉴 아이디와 이름
     */
    public MenuPickDto.UpdateResponse updatePickMenu(Long userId, Long pickId, MenuPickDto.UpdateRequest request) {
        Long menuId = request.menuId();
        // 유저 찾고, 변경하고자 하는 사람과 일치하는 지 확인
        User user = userReader.getById(userId);

        // 선택했던 메뉴 정보 가져오기
        UserMenuPick userMenuPick = getUserMenuPick(pickId, user);

        // 교체할 메뉴 찾기
        Menu menu = getMenu(menuId);

        // 메뉴 선택 연결 테이블 외래키 변경
        if (userMenuPick.getMenu() == menu) {
            throw new BusinessException(ErrorCode.MENU_PICK_NOT_CHANGED);
        }

        userMenuPick.update(menu);

        return MenuPickDto.UpdateResponse.builder()
                .menuId(menuId)
                .menuName(menu.getMenuName())
                .build();
    }

    /**
     * 메뉴 가져오기
     *
     * @param menuId 메뉴 아이디
     * @return 메뉴
     */
    private @NonNull Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    /**
     * 메뉴 선택 객체
     *
     * @param pickId 메뉴 아이디
     * @param user   유저
     * @return 메뉴 선택
     */
    private @NonNull UserMenuPick getUserMenuPick(Long pickId, User user) {
        UserMenuPick userMenuPick = userMenuPickRepository.findByMenuIdAndUser(pickId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메뉴 선택 내역이 존재하지 않습니다."));

        // 이미 확정 되었다면 변경 불가능
        if (userMenuPick.getStatus() == UserMenuPickStatus.USED) {
            throw new BusinessException(ErrorCode.MENU_PICK_ALREADY_USED);
        }
        return userMenuPick;
    }

    /**
     * 메뉴 선택 삭제
     *
     * @param userId 유저 아이디
     * @param pickId 선택한 메뉴
     * @return 메뉴 아이디
     */
    public MenuPickDto.DeleteResponse deletePickMenu(Long userId, Long pickId) {
        // 유저 찾기
        User user = userReader.getById(userId);

        // 유저, 메뉴 아이디와 맞는 테이블 찾아 제거
        UserMenuPick userMenuPick = getUserMenuPick(pickId, user);

        Long menuId = userMenuPick.getMenu().getId();
        userMenuPickRepository.delete(userMenuPick);

        Long count = 1L;
        PickCountHistory pickCountHistory = PickCountHistory.refund(user, count, userMenuPick.getTransactionId());

        UserPickCount userPickCount = userPickCountRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        pickCountHistoryRepository.save(pickCountHistory);
        userPickCount.restoreCount(count);

        return MenuPickDto.DeleteResponse.builder()
                .menuId(menuId)
                .build();
    }

    /**
     * ai식단 생성 시 실행
     *
     * @param userId  유저 id
     * @param request 요청 데이터
     * @return UUID, 식단 생성 상태
     */
    public DietGenerationDto.GenerateResponse requestGeneration(
            Long userId,
            DietGenerationDto.GenerateRequest request
    ) {
        User user = userReader.getById(userId);
        // 중복 제거를 위해 락 걸기
        Family family = familyRepository.findByIdForUpdate(user.getFamily().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));

        YearMonth targetMonth = request.targetMonth();
        validateTargetMonth(targetMonth);
        DietPeriod period = calculateDietPeriod(targetMonth);

        LocalDate targetMonthDate = targetMonth.atDay(1);
        LocalDate startDate = period.startDate();
        LocalDate endDate = period.endDate();
        validateGenerationRequest(family, startDate, endDate, targetMonthDate);

        // 유저 선택한 메뉴 전부 사용으로 변경
        List<UserMenuPick> userMenuPicks = userMenuPickRepository.findAllPendingForUpdate(family, targetMonthDate, UserMenuPickStatus.PENDING);

        List<Long> userMenuPickIds = userMenuPicks.stream()
                .map(UserMenuPick::getId)
                .toList();

        userMenuPicks.forEach(UserMenuPick::used);

        DietGeneration generation = DietGeneration.createPending(
                family,
                startDate,
                endDate,
                request.dailyMealCount(),
                targetMonthDate
        );

        DietGeneration saveGeneration = dietGenerationRepository.save(generation);

        applicationEventPublisher.publishEvent(DietGenerationRequestedEvent.builder()
                .userId(userId)
                .generationId(saveGeneration.getId())
                .request(request)
                .startDate(startDate)
                .endDate(endDate)
                .userMenuPickIds(userMenuPickIds)
                .build());

        return DietGenerationDto.GenerateResponse.builder()
                .generationId(generation.getId())
                .status(generation.getStatus())
                .build();
    }

    /**
     * 유효한 달인지 검사
     *
     * @param targetMonth 선택한 달
     */
    private void validateTargetMonth(YearMonth targetMonth) {
        YearMonth now = YearMonth.now();
        YearMonth nextMonth = now.plusMonths(1);

        if (targetMonth.isBefore(now) || targetMonth.isAfter(nextMonth)) {
            throw new BusinessException(ErrorCode.INVALID_TARGET_MONTH);
        }
    }

    /**
     * 기간 계산
     *
     * @param targetMonth 선택한 달
     * @return 시작일, 종료일
     */
    private DietPeriod calculateDietPeriod(YearMonth targetMonth) {
        YearMonth now = YearMonth.now();

        LocalDate startDate = targetMonth.equals(now)
                ? LocalDate.now()
                : targetMonth.atDay(1);

        LocalDate endDate = targetMonth.atEndOfMonth();

        return DietPeriod.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Builder
    private record DietPeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    /**
     * 식단 요청 유효한지 검증
     *
     * @param family    가족
     * @param startDate 시작
     * @param endDate   종료 날짜
     */
    private void validateGenerationRequest(
            Family family,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate targetMonthDate
    ) {
        List<DietGenerationStatus> activeStatuses = List.of(
                DietGenerationStatus.PENDING,
                DietGenerationStatus.PROCESSING,
                DietGenerationStatus.COMPLETED
        );

        // 사이 기간 중 생성된 식단이 있는지 확인
        boolean alreadyExists = dietGenerationRepository.existsOverlappingGeneration(
                family,
                startDate,
                endDate,
                activeStatuses
        );

        if (alreadyExists) {
            throw new BusinessException(ErrorCode.DIET_ALREADY_GENERATED);
        }

        // 기간 동안 몇 번 생성 했는지 확인
        long monthlyCount = dietGenerationRepository.countByFamilyAndTargetMonthAndStatusIn(
                family,
                targetMonthDate,
                activeStatuses
        );

        // 2번 제한
        if (monthlyCount >= 2) {
            throw new BusinessException(ErrorCode.DIET_GENERATION_MONTHLY_LIMIT_EXCEEDED);
        }

    }

    /**
     * 만들어진 식단 가져오기
     *
     * @param userId 유저 아이디
     * @param month  달
     * @return 식단 데이터
     */
    @Transactional(readOnly = true)
    public DietDto.ListItemResponse getDiets(Long userId, YearMonth month) {
        User user = userReader.getById(userId);
        Family family = user.getFamily();

        List<Diet> diets = getDiets(month, family);

        if (diets.isEmpty()) {
            return DietDto.ListItemResponse.builder()
                    .month(month)
                    .totalDays(0)
                    .isGenerated(false)
                    .build();
        }

        List<DietDto.DietResponse> dietResponses = getDietResponses(diets);

        int totalDays = dietResponses.size();

        return DietDto.ListItemResponse.builder()
                .month(month)
                .totalDays(totalDays)
                .diets(dietResponses)
                .isGenerated(true)
                .build();
    }

    /**
     * 식단 정보 데이터
     *
     * @param month  달
     * @param family 가족
     * @return 해당 달의 식단 데이터
     */
    private List<Diet> getDiets(YearMonth month, Family family) {
        // 해당 달의 시작 날짜와 마지막 날짜를 가져와 검사
        YearMonth yearMonth = YearMonth.from(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // family와 날짜를 기준으로 생성한 식단이 존재하는지 확인
        return dietRepository.findMonthlyDiets(family, startDate, endDate);
    }

    /**
     * 같은 날짜 별로 식단을 묶어서 식단 리스트 정보로 변환
     *
     * @param diets 식단
     * @return 일별 식단 정보
     */
    private static @NonNull List<DietDto.DietResponse> getDietResponses(List<Diet> diets) {
        Map<LocalDate, List<Diet>> dietsByDate = diets.stream()
                .collect(Collectors.groupingBy(
                        Diet::getMealDate,
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<DietDto.DietResponse> dietResponses = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Diet>> entry : dietsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Diet> dailyDiets = entry.getValue();

            List<DietDto.MealResponse> meals = new ArrayList<>();

            for (Diet diet : dailyDiets) {
                Menu menu = diet.getMenu();

                DietDto.MealResponse meal = DietDto.MealResponse.builder()
                        .dietId(diet.getId())
                        .mealType(diet.getMealType())
                        .dishType(menu.getDishType())
                        .menuName(menu.getMenuName())
                        .build();

                meals.add(meal);
            }
            DietDto.DietResponse dietResponse = DietDto.DietResponse.builder()
                    .date(date)
                    .meals(meals)
                    .build();

            dietResponses.add(dietResponse);
        }
        return dietResponses;
    }

    /**
     * 일일 식단 데이터를 가져옴
     *
     * @param userId 유저 아이디
     * @param date   날짜
     * @return 해당 날짜 메뉴, 재료, 영양 정보
     */
    @Transactional(readOnly = true)
    public DietDto.DailyDetailResponse getDailyMeals(Long userId, LocalDate date) {
        User user = userReader.getById(userId);
        Family family = user.getFamily();
        // 해당 날짜 가족 식단 전부 가져오기
        List<Diet> diets = dietRepository.findAllFamilyAndMealDate(family, date);

        if (diets.isEmpty()) {
            throw new BusinessException(ErrorCode.DIET_NOT_FOUND, "해당 날짜에 등록된 식단이 없습니다.");
        }

        // 아침 점심 저녁과 메뉴 연결
        Map<MealType, List<DietDto.MenuItemResponse>> menuItemsByMealType = new HashMap<>();
        // 초기화
        DailyNutritionTotal total = DailyNutritionTotal.builder().build();
        // 총 재료
        Map<String, DietDto.IngredientsResponse> totalIngredientMap = new LinkedHashMap<>();

        for (Diet diet : diets) {
            // 식단에 속해있는 메뉴 가져오기
            Menu menu = diet.getMenu();
            // 연결된 재료 전부 가져옴 메뉴 정보 추가
            DietDto.MenuItemResponse menuItemResponse = getMenuItemResponses(menu, totalIngredientMap, diet.getSource());
            // 새로운 키마다 리스트 생성
            menuItemsByMealType
                    .computeIfAbsent(diet.getMealType(), mealType -> new ArrayList<>())
                    .add(menuItemResponse);
            // 메뉴 영양 정보 더하기
            total.add(menu);
        }

        List<DietDto.DailyMealResponse> meals = toDailyMealResponse(menuItemsByMealType);

        List<DietDto.IngredientsResponse> totalIngredients =
                new ArrayList<>(totalIngredientMap.values());

        return DietDto.DailyDetailResponse.builder()
                .date(date)
                .totalCalories(total.getCalories())
                .totalCarbs(total.getCarbs())
                .totalProtein(total.getProtein())
                .totalFat(total.getFat())
                .totalSodium(total.getSodium())
                .meals(meals)
                .totalIngredients(totalIngredients)
                .build();
    }

    /**
     * MealType에 따라 메뉴 묶기
     *
     * @param menuItemsByMealType 맵
     * @return 끼니별 칼로리 합산
     */
    private @NonNull List<DietDto.DailyMealResponse> toDailyMealResponse(Map<MealType, List<DietDto.MenuItemResponse>> menuItemsByMealType) {
        // 아점저마다 식단 묶기
        return menuItemsByMealType.entrySet().stream()
                .map(entry -> {
                    MealType mealType = entry.getKey();
                    List<DietDto.MenuItemResponse> menuItems = entry.getValue();

                    BigDecimal mealCalories = menuItems.stream()
                            .map(DietDto.MenuItemResponse::kcal)
                            .map(this::nullToZero)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DietDto.DailyMealResponse.builder()
                            .mealType(mealType)
                            .mealCalories(mealCalories)
                            .meals(menuItems)
                            .build();
                })
                .toList();
    }

    /**
     * 재료 변환
     *
     * @param menu               메뉴
     * @param totalIngredientMap 재료 양 : 단위 , 재료 정보
     * @param source             누가 선택한 식단 인지
     * @return 합산 재료
     */
    private DietDto.MenuItemResponse getMenuItemResponses(
            Menu menu,
            Map<String, DietDto.IngredientsResponse> totalIngredientMap,
            DietMenuSource source
    ) {
        List<MenuIngredient> menuIngredients = menuIngredientRepository.findAllByMenuWithIngredient(menu);

        // 재료 이름과 양 단위 꺼내기
        List<DietDto.IngredientsResponse> requiredIngredients = menuIngredients.stream()
                .map(menuIngredient -> {
                    Ingredient ingredient = menuIngredient.getIngredient();

                    // 맵에 추가
                    addTotalIngredient(
                            totalIngredientMap,
                            ingredient,
                            menuIngredient.getQuantity(),
                            menuIngredient.getUnit()
                    );

                    return DietDto.IngredientsResponse.builder()
                            .ingredientId(ingredient.getId())
                            .name(ingredient.getName())
                            .quantity(menuIngredient.getQuantity())
                            .unit(menuIngredient.getUnit())
                            .build();
                })
                .toList();

        boolean familyChoice = source == DietMenuSource.USER_PICKED;
        return DietDto.MenuItemResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .dishType(menu.getDishType())
                .kcal(menu.getKcal())
                .carbs(menu.getCarbs())
                .protein(menu.getProtein())
                .fat(menu.getFat())
                .sodium(menu.getSodium())
                .requiredIngredients(requiredIngredients)
                .familyChoice(familyChoice)
                .build();
    }

    /**
     * 재료 총합 계산
     *
     * @param totalIngredientMap 총 재료 맵
     * @param ingredient         재료
     * @param quantity           양
     * @param unit               단위
     */
    private void addTotalIngredient(
            Map<String, DietDto.IngredientsResponse> totalIngredientMap,
            Ingredient ingredient,
            BigDecimal quantity,
            IngredientUnit unit
    ) {
        // 재료 아이디와 단위가 같은 경우
        String key = ingredient.getId() + ":" + unit;

        DietDto.IngredientsResponse newValue = DietDto.IngredientsResponse.builder()
                .ingredientId(ingredient.getId())
                .name(ingredient.getName())
                .quantity(quantity)
                .unit(unit)
                .build();

        // 합산,
        totalIngredientMap.merge(
                key,
                newValue,
                (oldValue, value) ->
                        DietDto.IngredientsResponse.builder()
                                .ingredientId(oldValue.ingredientId())
                                .name(oldValue.name())
                                .quantity(addNullable(oldValue.quantity(), value.quantity()))
                                .unit(oldValue.unit())
                                .build()
        );
    }

    /**
     * BigDecimal 계산
     *
     * @param a 숫자
     * @param b 숫자
     * @return null이 아니라면 더함
     */
    private BigDecimal addNullable(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }

        return a.add(b);
    }

    /**
     * null인지 확인
     *
     * @param value 값
     * @return null이면 zero반환
     */
    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Builder
    @Getter
    private static class DailyNutritionTotal {
        @Builder.Default
        private BigDecimal calories = BigDecimal.ZERO;
        @Builder.Default
        private BigDecimal carbs = BigDecimal.ZERO;
        @Builder.Default
        private BigDecimal protein = BigDecimal.ZERO;
        @Builder.Default
        private BigDecimal fat = BigDecimal.ZERO;
        @Builder.Default
        private BigDecimal sodium = BigDecimal.ZERO;

        void add(Menu menu) {
            calories = calories.add(nullToZero(menu.getKcal()));
            carbs = carbs.add(nullToZero(menu.getCarbs()));
            protein = protein.add(nullToZero(menu.getProtein()));
            fat = fat.add(nullToZero(menu.getFat()));
            sodium = sodium.add(nullToZero(menu.getSodium()));
        }

        private static BigDecimal nullToZero(BigDecimal value) {
            return value == null ? BigDecimal.ZERO : value;
        }
    }

    /**
     * ai생성된 메뉴 교체
     *
     * @param userId  리더 아이디
     * @param dietId  식단 아이디
     * @param request 교체할 메뉴 아이디
     * @return 교체한 메뉴 id, 메뉴 이름
     */
    public DietMenuDto.ReplaceResponse replaceMenu(Long userId, Long dietId, DietMenuDto.ReplaceRequest request) {
        User user = userReader.getById(userId);

        Diet diet = getDiet(dietId);

        if(diet.getSource() == DietMenuSource.USER_PICKED){
            throw new BusinessException(ErrorCode.DIET_MENU_LOCKED);
        }

        // 내 가족 식단이 아닐 경우
        checkFamilyLeader(user, diet);

        Menu menu = getMenu(request.menuId());

        diet.replaceMenu(menu);

        return DietMenuDto.ReplaceResponse.builder()
                .replacedMenuId(menu.getId())
                .menuName(menu.getMenuName())
                .build();
    }

    /**
     * 가족 리더 인지 확인
     *
     * @param user 유저
     * @param diet 식단
     */
    private void checkFamilyLeader(User user, Diet diet) {
        if (user.getFamily() != diet.getFamily()) {
            throw new BusinessException(ErrorCode.NOT_FAMILY_MEMBER);
        }
    }

    /**
     * 식단 가져오기
     *
     * @param dietId 식단 아이디
     * @return 식단
     */
    private @NonNull Diet getDiet(Long dietId) {
        return dietRepository.findById(dietId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIET_ITEM_NOT_FOUND));
    }

    /**
     * 대체 메뉴 리스트
     *
     * @param userId   유저 아이디
     * @param dietId   식단 아이디
     * @param keyword  키워드
     * @param pageable 페이지
     * @return 메뉴 리스트
     */
    @Transactional(readOnly = true)
    public DietMenuDto.ReplacementMenuListResponse replacementMenus(
            Long userId, Long dietId, String keyword, Pageable pageable
    ) {
        // 유저 찾기
        User user = userReader.getById(userId);
        // 내 가족 식단 인지 확인
        Diet diet = getDiet(dietId);

        checkFamilyLeader(user, diet);

        // USER_PICKED 식단이면 교체 불가
        checkUserMenuPick(diet);

        String normalizedKeyword = normalizeKeyword(keyword);
        // 현재 식단의 dishType이 같은 메뉴만 조회
        Menu menu = diet.getMenu();

        // 키워드가 있다면 키워드도 조회
        Page<Menu> menuPage = menuRepository.searchReplacementMenus(menu.getCategory(), menu.getDishType(), menu.getId(), normalizedKeyword, pageable);
        List<DietMenuDto.ReplacementMenuResponse> menuInfoList = menuPage.stream()
                .map(DietMenuDto.ReplacementMenuResponse::from)
                .toList();

        DietMenuDto.PageInfoResponse pageInfo = DietMenuDto.PageInfoResponse.builder()
                .currentPage(menuPage.getNumber() + 1)
                .totalPages(menuPage.getTotalPages())
                .totalElements(menuPage.getTotalElements())
                .build();

        return DietMenuDto.ReplacementMenuListResponse.builder()
                .dietId(dietId)
                .keyword(keyword)
                .dishType(menu.getDishType())
                .menus(menuInfoList)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * 공백 제거
     *
     * @param keyword 키워드
     * @return 키워드
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    /**
     * 교체할 메뉴 상세 정보
     *
     * @param userId 유저 아이디
     * @param dietId 식단 아이디
     * @param menuId 메뉴 아이디
     * @return 메뉴 상세 정보
     */
    @Transactional(readOnly = true)
    public DietMenuDto.MenuDetailsResponse menuDetails(Long userId, Long dietId, Long menuId) {
        // 유저 확인
        User user = userReader.getById(userId);
        // 가족 식단인지 확인
        Diet diet = getDiet(dietId);
        checkFamilyLeader(user, diet);
        // 유저가 선택한 식단인지 확인
        checkUserMenuPick(diet);
        // 메뉴 찾기
        Menu menu = getMenu(menuId);

        List<MenuIngredient> menuIngredients = menuIngredientRepository.findAllByMenuWithIngredient(menu);
        // 재료 리스트 불러오기
        List<DietMenuDto.IngredientsResponse> ingredients = menuIngredients.stream()
                .map(menuIngredient -> {
                    Ingredient ingredient = menuIngredient.getIngredient();
                    return DietMenuDto.IngredientsResponse.builder()
                            .ingredientId(ingredient.getId())
                            .name(ingredient.getName())
                            .quantityText(menuIngredient.getQuantityText())
                            .build();
                })
                .toList();

        return DietMenuDto.MenuDetailsResponse.builder()
                .dietId(dietId)
                .menuId(menuId)
                .menuName(menu.getMenuName())
                .dishType(menu.getDishType())
                .kcal(menu.getKcal())
                .carbs(menu.getCarbs())
                .protein(menu.getProtein())
                .fat(menu.getFat())
                .sodium(menu.getSodium())
                .requiredIngredients(ingredients)
                .build();
    }

    /**
     * 유저가 픽한 메뉴인지 확인
     *
     * @param diet 식단
     */
    private static void checkUserMenuPick(Diet diet) {
        if (DietMenuSource.USER_PICKED == diet.getSource()) {
            throw new BusinessException(ErrorCode.DIET_MENU_LOCKED);
        }
    }

    /**
     * 추천 메뉴
     *
     * @param userId 유저 아이디
     * @param dietId 식단 아이디
     * @return 3개 메뉴
     */
    @Transactional(readOnly = true)
    public DietMenuDto.RecommendationResponse recommendations(Long userId, Long dietId) {
        User user = userReader.getById(userId);
        Diet diet = getDiet(dietId);

        if (diet.getSource() == DietMenuSource.USER_PICKED) {
            throw new BusinessException(ErrorCode.DIET_MENU_LOCKED);
        }

        checkFamilyLeader(user, diet);
        Family family = user.getFamily();

        Menu menu = diet.getMenu();
        // 알러지 정보 조회
        List<DietMenuDto.CandidateResponse> candidateResponses = getCandidateResponses(family, menu);

        return DietMenuDto.RecommendationResponse.builder()
                .menuName(menu.getMenuName())
                .dishType(menu.getDishType())
                .menus(candidateResponses)
                .build();
    }

    /**
     * 알러지 제외하고 후보 메뉴 뽑기
     *
     * @param family 가족
     * @param menu   메뉴
     * @return 후보 메뉴
     */
    private @NonNull List<DietMenuDto.CandidateResponse> getCandidateResponses(Family family, Menu menu) {
        List<User> users = userRepository.findAllFamily(family);

        List<UserIngredientPreference> familyPreferences =
                userIngredientPreferenceRepository.findAllByUserInFetchIngredient(users);

        Set<Long> allergyIngredientIds = familyPreferences.stream()
                .filter(preference -> preference.getPreference() == FoodPreferenceType.ALLERGY)
                .map(preference -> preference.getIngredient().getId())
                .collect(Collectors.toSet());

        List<Menu> candidates;

        if (allergyIngredientIds.isEmpty()) {
            candidates = menuRepository.findRecommendationCandidates(
                    menu.getCategory(),
                    menu.getDishType(),
                    menu.getId()
            );
        } else {
            candidates = menuRepository.findRecommendationCandidatesWithoutAllergy(
                    menu.getCategory(),
                    menu.getDishType(),
                    menu.getId(),
                    allergyIngredientIds
            );
        }

        List<Menu> shuffledCandidates = new ArrayList<>(candidates);
        Collections.shuffle(shuffledCandidates);
        List<Menu> recommendedMenus = shuffledCandidates.stream()
                .limit(3)
                .toList();

        return recommendedMenus.stream()
                .map(candidate -> {
                    List<DietMenuDto.IngredientResponse> ingredientResponses =
                            menuIngredientRepository.findAllByMenuWithIngredient(candidate).stream()
                                    .map(menuIngredient ->
                                            DietMenuDto.IngredientResponse.builder()
                                                    .name(menuIngredient.getIngredient().getName())
                                                    .build()
                                    )
                                    .toList();

                    return DietMenuDto.CandidateResponse.builder()
                            .menuId(candidate.getId())
                            .menuName(candidate.getMenuName())
                            .kcal(candidate.getKcal())
                            .carbs(candidate.getCarbs())
                            .protein(candidate.getProtein())
                            .fat(candidate.getFat())
                            .sodium(candidate.getSodium())
                            .ingredients(ingredientResponses)
                            .build();
                })
                .toList();
    }

}
