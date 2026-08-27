package kongju.pickmeal.core.user;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static kongju.pickmeal.support.fixture.UserFixture.user;


public class UserTest {
    @Test
    @DisplayName("유저 생일 업데이트")
    void user_birthday_update() {
        User user = user();

        user.updateBirthDate(LocalDate.of(2000, 5, 30));

        assertThat(user.getBirthDate()).isEqualTo("2000-05-30");
    }
}
