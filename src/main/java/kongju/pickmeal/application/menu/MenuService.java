package kongju.pickmeal.application.menu;

import java.util.List;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.application.menu.data.MenuDto.*;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.menu.repository.IngredientRepository;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;
import kongju.pickmeal.application.menu.data.MenuFilterDto.CategoryResponse;
import kongju.pickmeal.application.menu.data.MenuFilterDto.DishTypeResponse;
import kongju.pickmeal.application.menu.data.MenuFilterDto.MetadataResponse;
import kongju.pickmeal.application.menu.data.FamilyCustomMenuDto.SaveRequest;
import kongju.pickmeal.application.menu.data.FamilyCustomMenuDto.IngredientRequest;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final UserReader userReader;

    /**
     * 필터링 카테고리 목록 가저오기
     *
     * @return 카테고리, 디시타입 리스트
     */
    public MetadataResponse getFilterMetadata() {
        List<CategoryResponse> categories = Arrays.stream(MenuCategory.values())
                .map(category -> CategoryResponse.builder()
                        .name(category)
                        .displayName(category.getDisplayName())
                        .build()
                )
                .toList();

        List<DishTypeResponse> dishTypes = Arrays.stream(DishType.values())
                .map(dishType -> DishTypeResponse.builder()
                        .name(dishType)
                        .displayName(dishType.getDisplayName())
                        .build()
                )
                .toList();

        return MetadataResponse.builder()
                .categories(categories)
                .dishTypes(dishTypes)
                .build();
    }

    /**
     * 메뉴 찾기
     *
     * @param category 카테고리
     * @param dishType 요리 타입
     * @param keyword  키워드
     * @param pageable 페이지
     * @return 검색 결과
     */
    public ListItemResponse searchMenus(
            MenuCategory category, DishType dishType, String keyword, Pageable pageable
    ) {
        String nKeyword = normalizeKeyword(keyword);

        // 카테고리나 디쉬 타입이 있다면 쿼리로 불러오기
        Page<Menu> menuPage = menuRepository.searchByFilters(category, dishType, nKeyword, pageable);

        List<MenuInfoResponse> menuInfoList = menuPage.stream()
                .map(menu -> MenuInfoResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getMenuName())
                        .category(menu.getCategory())
                        .dishType(menu.getDishType())
                        .kcal(menu.getKcal())
                        .build()
                )
                .toList();

        PageInfoResponse pageInfo = PageInfoResponse.builder()
                .currentPage(menuPage.getNumber() + 1)
                .totalPages(menuPage.getTotalPages())
                .totalElements(menuPage.getTotalElements())
                .build();

        return ListItemResponse.builder()
                .content(menuInfoList)
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
     * 메뉴 상세 내용
     *
     * @param menuId 메뉴 아이디
     * @return 메뉴 상세 정보
     */
    public DetailResponse detailMenu(Long menuId) {
        // id로 메뉴 찾기
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_MENU_ID));

        List<MenuIngredient> menuIngredients = menuIngredientRepository.findAllByMenuWithIngredient(menu);

        // 메뉴 재료 정보 제공
        List<IngredientResponse> ingredients = menuIngredients.stream()
                .map(menuIngredient -> IngredientResponse.builder()
                        .ingredientName(menuIngredient.getIngredient().getName())
                        .quantityText(menuIngredient.getQuantityText())
                        .build()
                )
                .toList();

        // 메뉴 영양 정보
        return DetailResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .category(menu.getCategory())
                .dishType(menu.getDishType())
                .kcal(menu.getKcal())
                .carbs(menu.getCarbs())
                .fat(menu.getFat())
                .sodium(menu.getSodium())
                .protein(menu.getProtein())
                .ingredients(ingredients)
                .build();
    }

    /**
     * 가족 메뉴 추가
     *
     * @param userId  유저 아이디
     * @param request 메뉴
     */
    @Transactional
    public void createMenu(Long userId, SaveRequest request) {
        User user = userReader.getById(userId);

        checkFamily(user);

        Menu menu = Menu.createFamilyMenu(
                request.menuName(),
                request.category(),
                request.dishType(),
                request.kcal(),
                request.carbs(),
                request.protein(),
                request.fat(),
                request.sodium(),
                user.getFamily());

        List<IngredientRequest> ingredientRequests = request.ingredients();

        Menu savedMenu = menuRepository.save(menu);

        List<MenuIngredient> menuIngredients = getOrCreateIngredient(savedMenu, ingredientRequests);
        menuIngredientRepository.saveAll(menuIngredients);
    }

    /**
     * 가족 여부 확인
     * @param user 유저
     */
    private static void checkFamily(User user) {
        if (user.getFamily() == null) {
            throw new BusinessException(ErrorCode.FAMILY_NOT_FOUND);
        }
    }

    /**
     * 재료 저장, 메뉴 재료 연결 테이블 생성
     * @param menu 메뉴
     * @param ingredientRequests 재료 정보
     * @return 메뉴 재료 연결 테이블
     */
    private List<MenuIngredient> getOrCreateIngredient(Menu menu, List<IngredientRequest> ingredientRequests) {
        return ingredientRequests.stream()
                .map(ingredientRequest -> {
                    Ingredient ingredient;
                    if (ingredientRequest.ingredientId() != null) {
                        ingredient = ingredientRepository.findById(ingredientRequest.ingredientId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));
                    }else{
                        String ingredientName = normalizeIngredientName(ingredientRequest.ingredientName());

                        ingredient = ingredientRepository.findByName(ingredientName)
                                .orElseGet(() -> ingredientRepository.save(Ingredient.create(ingredientName)));
                    }
                    String quantityText = ingredientRequest.quantity() + " " + ingredientRequest.unit();
                    return MenuIngredient.create(menu, ingredient, quantityText, ingredientRequest.quantity(),
                            ingredientRequest.unit(), ingredientRequest.type());
                })
                .toList();
    }

    /**
     * 재료 이름 확인
     * @param name 이름
     * @return 공백 제거한 재료 이름
     */
    private String normalizeIngredientName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return name.trim();
    }

    /**
     * 메뉴 수정
     * @param userId 유저 아이디
     * @param menuId 메뉴 아이디
     * @param request 메뉴 정보
     */
    @Transactional
    public void updateCustomMenu(Long userId, Long menuId, SaveRequest request) {
        User user = userReader.getById(userId);
        // 메뉴
        Menu menu = getMenu(menuId);

        checkFamily(user);

        checkMyFamily(menu.getFamily(), user.getFamily());

        menu.update(request.menuName(),
                request.category(),
                request.dishType(),
                request.kcal(),
                request.carbs(),
                request.protein(),
                request.fat(),
                request.sodium());

        // 기존 재료, 메뉴 연결 테이블 삭제
        menuIngredientRepository.deleteAllByMenu(menu);

        // 새로 추가
        List<IngredientRequest> ingredientRequests = request.ingredients();

        List<MenuIngredient> menuIngredients = getOrCreateIngredient(menu, ingredientRequests);
        menuIngredientRepository.saveAll(menuIngredients);
    }

    /**
     * 내 가족 메뉴인지 확인
     * @param menuFamily 메뉴 가족
     * @param userFamily 리더 가족
     */
    private static void checkMyFamily(Family menuFamily, Family userFamily) {
        if(menuFamily != userFamily) {
            throw new BusinessException(ErrorCode.NOT_YOUR_FAMILY_REQUEST);
        }
    }

    /**
     * 메뉴 찾기
     * @param menuId 메뉴 아이디
     * @return 메뉴
     */
    private @NonNull Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    /**
     * 커스텀 메뉴 삭제
     * @param userId 유저 아이디
     * @param menuId 메뉴 아이디
     */
    @Transactional
    public void deleteCustomMenu(Long userId, Long menuId) {
        User user = userReader.getById(userId);
        checkFamily(user);

        Menu menu = getMenu(menuId);
        checkMyFamily(user.getFamily(), menu.getFamily());

        menuIngredientRepository.deleteAllByMenu(menu);
        menuRepository.delete(menu);
    }
}
