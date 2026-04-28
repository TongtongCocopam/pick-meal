package kongju.pickmeal.fixture;

import kongju.pickmeal.application.user.data.MemberRequest;

import java.time.LocalDate;

public class MemberFixture {
    public static MemberRequest.Register createRequest(String loginId, String password, String passwordCheck, String email, String name, String nickName, LocalDate birthDate) {
        return MemberRequest.Register.builder()
                .loginId(loginId)
                .password(password)
                .passwordCheck(passwordCheck)
                .email(email)
                .nickName(nickName)
                .birthDate(birthDate)
                .build();
    }

    public static MemberRequest.Register createRequest() {
        return MemberRequest.Register.builder()
                .loginId("test1234")
                .password("test0000!!")
                .passwordCheck("test0000!!")
                .email("test@test.com")
                .nickName("tester")
                .birthDate(LocalDate.now())
                .build();
    }
}
