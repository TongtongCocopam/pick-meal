package kongju.pickmeal.support.fixture;

import kongju.pickmeal.core.user.User;

public class UserFixture {
    public static User user() {
        return User.builder()
                .loginId("testUser")
                .nickname("tester")
                .password("password1234")
                .email("test1234@gmail.com")
                .build();
    }

    public static User user(String loginId, String email, String nickname, String password) {
        return User.builder()
                .loginId(loginId)
                .email(email)
                .nickname(nickname)
                .password(password)
                .build();
    }
}
