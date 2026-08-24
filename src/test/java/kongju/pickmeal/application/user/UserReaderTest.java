package kongju.pickmeal.application.user;

import java.util.Optional;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.user.repository.UserRepository;

import static kongju.pickmeal.support.fixture.UserFixture.user;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class UserReaderTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserReader userReader;

    @Test
    @DisplayName("유저가 없는 경우")
    void should_fail_user_search_when_user_not_exist() {
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userReader.getById(userId)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("성공케이스")
    void should_success_user_search() {
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.of(user()));

        User user = userReader.getById(userId);

        assertThat(user.getNickname()).isEqualTo("testUser");
    }

}
