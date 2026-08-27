package kongju.pickmeal.core.user;


import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;

import static kongju.pickmeal.support.fixture.UserFixture.user;


public class PickCountHistoryTest {
    @Test
    @DisplayName("선택권 수량 확인")
    void 선택권이_null_인경우() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> {
                    PickCountHistory.credit(
                            user(),
                            null,
                            UUID.randomUUID()
                    );
                });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
        assertThat(exception.getDetailMessage()).isEqualTo("선택권 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("선택권 수량 음수")
    void 선택권이_음수인_경우() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> {
                    PickCountHistory.credit(
                            user(),
                            -1L,
                            UUID.randomUUID()
                    );
                });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
        assertThat(exception.getDetailMessage()).isEqualTo("선택권 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("선택권 수량 양수")
    void 선택권이_양수인_경우() {
        PickCountHistory pickCountHistory = PickCountHistory.credit(
                user(),
                1L,
                UUID.randomUUID());

        assertThat(pickCountHistory.getCount()).isEqualTo(1L);
    }

}
