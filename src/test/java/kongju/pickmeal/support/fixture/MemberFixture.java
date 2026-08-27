package kongju.pickmeal.support.fixture;

import java.time.LocalDate;

import kongju.pickmeal.application.user.data.UserDto;


public class MemberFixture {
    public static UserDto.SignupRequest createRequest(String loginId, String password, String passwordCheck, String email, String nickname, LocalDate birthDate) {
        return UserDto.SignupRequest.builder()
                .loginId(loginId)
                .password(password)
                .passwordCheck(passwordCheck)
                .email(email)
                .nickname(nickname)
                .birthDate(birthDate)
                .build();
    }

    public static UserDto.SignupRequest createRequest() {
        return UserDto.SignupRequest.builder()
                .loginId("test1234")
                .password("test0000!!")
                .passwordCheck("test0000!!")
                .email("test@test.com")
                .nickname("testUser")
                .birthDate(LocalDate.parse("2003-06-12"))
                .build();
    }
}
