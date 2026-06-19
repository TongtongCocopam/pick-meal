package kongju.pickmeal.application.diet;

import java.util.List;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.user.UserPickCount;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.UserReader;
import kongju.pickmeal.support.fixture.UserFixture;
import kongju.pickmeal.support.fixture.MenuFixture;
import kongju.pickmeal.core.diet.UserMenuPickRepository;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.menu.repository.MenuRepository;
import kongju.pickmeal.core.user.repository.UserPickCountRepository;
import kongju.pickmeal.core.user.repository.PickCountHistoryRepository;


@ExtendWith(SpringExtension.class)
public class DietServiceTest {
    @Mock
    private UserReader userReader;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private UserMenuPickRepository userMenuPickRepository;
    @Mock
    private UserPickCountRepository userPickCountRepository;
    @Mock
    private PickCountHistoryRepository pickCountHistoryRepository;

    @InjectMocks
    private DietService dietService;

    @Nested
    @DisplayName("선택권 사용")
    class UsePickCount {
        @Test
        @DisplayName("메뉴가 없을 겨우")
        public void should_fail_menu_pick_when_menu_not_found() {
            Long userId = 1L;

            User user = UserFixture.user();
            given(userReader.getById(userId)).willReturn(user);

            given(menuRepository.findById(any())).willReturn(Optional.empty());

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .build();

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> dietService.menuPick(userId, request));

            assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("탈퇴한 가족의 id일 경우")
        public void should_fail_menu_pick_when_not_my_family() {
            Long userId = 1L;

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .build();

            given(userReader.getById(any())).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> dietService.menuPick(userId, request));

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("선택권 개수 초과")
        public void should_fail_menu_pick_when_pick_count_exceed() {
            Long userId = 1L;

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Menu menu = MenuFixture.menu();
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            UserPickCount userPickCount = UserPickCount.initialize(user);
            given(userPickCountRepository.findByUser(any())).willReturn(Optional.of(userPickCount));

            given(pickCountHistoryRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> dietService.menuPick(userId, request));

            assertEquals(ErrorCode.TOO_MANY_SELECTIONS, exception.getErrorCode());

        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_menu_pick() {
            Long userId = 1L;

            MenuPickDto.CreateRequest request = MenuPickDto.CreateRequest.builder()
                    .menuIds(List.of(1L, 2L))
                    .build();
            // 선택권 사용 유저
            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            // 선택한 메뉴
            Menu menu1 = MenuFixture.menu();
            Menu menu2 = MenuFixture.menu();

            given(menuRepository.findById(1L)).willReturn(Optional.of(menu1));
            given(menuRepository.findById(2L)).willReturn(Optional.of(menu2));

            // 개수 부여
            UserPickCount userPickCount = UserPickCount.initialize(user);
            userPickCount.restoreCount(5L);

            // 개수 차감할 객체
            given(userPickCountRepository.findByUser(user)).willReturn(Optional.of(userPickCount));
            // 히스토리 저장
            given(pickCountHistoryRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            given(userMenuPickRepository.saveAll(anyList()))
                    .willReturn(
                            List.of(UserMenuPick.create(user, menu1),
                                    UserMenuPick.create(user, menu2)));

            MenuPickDto.CreateResponse response = dietService.menuPick(userId, request);

            assertEquals(2, response.pickedCount());
        }
    }

    @Nested
    @DisplayName("선택한 메뉴 변경")
    class UpdatePickMenu{
        @Test
        @DisplayName("유저 찾기 실패")
        public void should_fail_update_pick_menu_when_user_not_found() {
            Long userId = 1L;
            Long pickId = 2L;
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(1L)
                    .build();

            given(userReader.getById(any())).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("이전 선택했던 메뉴 정보 없음")
        public void should_fail_update_pick_menu_when_prev_choice_not_found() {
            Long userId = 1L;
            Long pickId = 1L;
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(1L)
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.MENU_PICK_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("메뉴 찾기 실패")
        public void should_fail_update_pick_menu_when_menu_not_found() {
            Long userId = 1L;
            Long pickId = 1L;
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(2L)
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Menu menu = MenuFixture.menu();
            UserMenuPick userMenuPick = UserMenuPick.create(user,menu);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.of(userMenuPick));

            given(menuRepository.findById(any())).willReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("이전과 동일한 메뉴")
        public void should_fail_update_pick_menu_when_not_change_menu() {
            Long userId = 1L;
            Long pickId = 1L;
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(1L)
                    .build();

            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            Menu menu = MenuFixture.menu();
            given(menuRepository.findById(pickId)).willReturn(Optional.of(menu));
            UserMenuPick userMenuPick = UserMenuPick.create(user,menu);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.of(userMenuPick));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> dietService.updatePickMenu(userId, pickId, request));

            assertEquals(ErrorCode.MENU_PICK_NOT_CHANGED, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 케이스")
        public void should_success_update_pick_menu() {
            Long userId = 1L;
            Long pickId = 1L;
            MenuPickDto.UpdateRequest request = MenuPickDto.UpdateRequest.builder()
                    .menuId(2L)
                    .build();

            // 유저
            User user = UserFixture.user();
            given(userReader.getById(any())).willReturn(user);

            // 교체할 메뉴
            Menu menu = MenuFixture.menu();
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            Menu menu1 = MenuFixture.menu("마라탕");
            UserMenuPick userMenuPick = UserMenuPick.create(user, menu1);
            given(userMenuPickRepository.findByMenuIdAndUser(pickId, user)).willReturn(Optional.of(userMenuPick));

            MenuPickDto.UpdateResponse response = dietService.updatePickMenu(userId, pickId, request);

            assertEquals(menu.getMenuName(), response.menuName());
        }
    }
}
