package kongju.pickmeal.application.diet;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.api.security.CustomUserDetails;
import kongju.pickmeal.core.diet.UserMenuPickRepository;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class DietService {
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final UserMenuPickRepository userMenuPickRepository;

    /**
     * 메뉴 선택
     *
     * @param userDetails 유저 정보
     * @param request     메뉴 선택 리스트
     * @return 메뉴 선택 정보
     */
    public MenuPickDto.CreateResponse menuPick(
            CustomUserDetails userDetails,
            MenuPickDto.CreateRequest request) {

        User user = userRepository.findById(userDetails.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Long> menuIds = request.menuIds();
        // 유저가 선택한 메뉴들을 유저 픽 연결 테이블에 넣기
        List<UserMenuPick> userMenuPickList = menuIds.stream()
                .map(menuId -> {
                    Menu menu = menuRepository.findById(menuId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

                    return UserMenuPick.builder()
                            .user(user)
                            .menu(menu)
                            .build();
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

}
