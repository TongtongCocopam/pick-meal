package kongju.pickmeal.core.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;

import static kongju.pickmeal.support.fixture.UserFixture.user;


public class UserPickCountTest {
    @Test
    @DisplayName("유저 선택권 사용 수량이 -1인 경우")
    void user_pick_count_unvalidated_count_minus() {
        User user = user();
        UserPickCount userPickCount = UserPickCount.initialize(user);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userPickCount.useCount(-1L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
        assertThat(exception.getDetailMessage()).isEqualTo("선택권 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("유저 선택권 사용 수량이 0인 경우")
    void user_pick_count_unvalidated_count_0() {
        User user = user();
        UserPickCount userPickCount = UserPickCount.initialize(user);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userPickCount.useCount(0L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
        assertThat(exception.getDetailMessage()).isEqualTo("선택권 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("유저 선택권 사용 수량이 1이상인 경우")
    void user_pick_count_validated_count() {
        User user = user();
        UserPickCount userPickCount = UserPickCount.initialize(user);
        userPickCount.restoreCount(2L);
        userPickCount.useCount(1L);
        assertThat(userPickCount.getRemainingPickCount()).isEqualTo(1L);
    }
}
