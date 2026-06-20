package kongju.pickmeal.application.diet;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.user.UserPickCount;
import kongju.pickmeal.core.user.PickCountHistory;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.core.diet.UserMenuPickRepository;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.user.repository.UserPickCountRepository;
import kongju.pickmeal.core.user.repository.PickCountHistoryRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class DietService {
    private final UserReader userReader;
    private final MenuRepository menuRepository;
    private final UserMenuPickRepository userMenuPickRepository;
    private final UserPickCountRepository userPickCountRepository;
    private final PickCountHistoryRepository pickCountHistoryRepository;

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

        List<Long> menuIds = request.menuIds();
        Long count = (long) menuIds.size();

        // 유저가 선택한 메뉴들을 유저 픽 연결 테이블에 넣기
        List<UserMenuPick> userMenuPickList = menuIds.stream()
                .map(menuId -> {
                    Menu menu = getMenu(menuId);

                    debitPickCount(user, count);
                    debitHistory(user, count, UUID.randomUUID());

                    return UserMenuPick.create(user, menu);
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
     * 선택권 차감
     *
     * @param user  사용 유저
     * @param count 개수
     */
    private void debitPickCount(User user, Long count) {
        UserPickCount userPickCount = userPickCountRepository.findByUser(user)
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
     * @param menuId 메뉴 아이디
     * @return 메뉴
     */
    private @NonNull Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    /**
     * 메뉴 선택 객체
     * @param pickId 메뉴 아이디
     * @param user 유저
     * @return 메뉴 선택
     */
    private @NonNull UserMenuPick getUserMenuPick(Long pickId, User user) {
        return userMenuPickRepository.findByMenuIdAndUser(pickId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메뉴 선택 내역이 존재하지 않습니다."));
    }

    /**
     * 메뉴 선택 삭제
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

        return MenuPickDto.DeleteResponse.builder()
                .menuId(menuId)
                .build();
    }

}
