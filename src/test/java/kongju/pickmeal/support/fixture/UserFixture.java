package kongju.pickmeal.support.fixture;

import java.time.LocalDate;

import kongju.pickmeal.core.user.User;


public class UserFixture {
    public static User user() {
        return User.create("testUser",
                LocalDate.parse("2003-06-01"),
                "tester", "test1234@gmail.com",
                "password1234");
    }

    public static User user(
            String nickname,
            String loginId,
            String email,
            String password) {
        return User.create(
                nickname,
                LocalDate.parse("2003-06-01"),
                loginId,
                email,
                password);
    }
}
