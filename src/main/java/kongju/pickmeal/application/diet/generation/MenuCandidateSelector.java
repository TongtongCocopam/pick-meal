package kongju.pickmeal.application.diet.generation;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.ai.AiDietGenerateDto;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;


@Component
@RequiredArgsConstructor
public class MenuCandidateSelector {
    private final MenuRepository menuRepository;
    private final MenuIngredientRepository menuIngredientRepository;

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
    public @NonNull List<AiDietGenerateDto.MenuCandidate> select(
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
        List<AiDietGenerateDto.MenuCandidate> result = new ArrayList<>();

        result.addAll(selectByDishTypeWithFallback(
                preferredCandidates,
                fallbackCandidates,
                DishType.SOUP,
                limit.soupLimit()
        ));

        result.addAll(selectByDishTypeWithFallback(
                preferredCandidates,
                fallbackCandidates,
                DishType.SIDE_DISH,
                limit.sideDishLimit()
        ));

        return result;
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
    public Map<Long, List<MenuIngredient>> getMenuIngredientMap(List<Menu> menus) {
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

        int soupLimit = Math.max(totalMealCount, 60);
        int sideDishLimit = Math.max(totalMealCount * 2, 120);

        return DishTypeCandidateLimit.builder()
                .totalMealCount(totalMealCount)
                .soupLimit(soupLimit)
                .sideDishLimit(sideDishLimit)
                .build();
    }

    @Builder
    private record DishTypeCandidateLimit(
            int totalMealCount,
            int soupLimit,
            int sideDishLimit
    ) {
    }

}
