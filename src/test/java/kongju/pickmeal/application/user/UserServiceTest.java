package kongju.pickmeal.application.user;

import kongju.pickmeal.application.user.data.MemberRequest;
import kongju.pickmeal.application.user.data.MemberResponse;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import kongju.pickmeal.core.user.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

//    @BeforeEach
//    public void setUp(){
//
//    }

    @Test
    @DisplayName("회원가입 시 중복된 아이디가 있으면 BusinessException을 던진다")
    public void should_fail_signup_when_id_is_duplicate() {
        //dto를 사용해서 데이터 주입
        MemberRequest.Register request = MemberRequest.Register.builder()
                .loginId("test1234")
                .password("test0000!!")
                .passwordCheck("test0000!!!")
                .email("test1234@gmail.com")
                .nickName("tester")
                .birthDate(LocalDate.parse("2000-03-03"))
                .build();
        // id 중복 확인 true로 함
        given(userRepository.existsByLoginId(any())).willReturn(true);

        // 회원가입 로직 실행
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("회원가입 시 중복된 이메일이 있으면 BusinessException을 던진다")
    public void should_fail_signup_when_email_is_duplicate() {
        //dto를 사용해서 데이터 주입
        MemberRequest.Register request = MemberRequest.Register.builder()
                .loginId("test1234")
                .password("test0000!!")
                .passwordCheck("test0000!!!")
                .email("test1234@gmail.com")
                .nickName("tester")
                .birthDate(LocalDate.parse("2000-03-03"))
                .build();
        // email 중복 확인 true로 함
        given(userRepository.existsByEmail(any())).willReturn(true);
        // 회원가입 로직 실행
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE);

    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 다르면 회원가입에 실패한다")
    void should_fail_when_password_mismatch() {
        // 비밀번호와 확인용 비밀번호를 다르게 설정
        MemberRequest.Register request = MemberRequest.Register.builder()
                .loginId("test1234")
                .password("test0000!!")
                .passwordCheck("wrong")
                .email("test1234@gmail.com")
                .nickName("tester")
                .birthDate(LocalDate.parse("2000-03-03"))
                .build();

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("회원가입 성공")
    public void should_success_signup() {
        // dto를 사용해서 데이터 주입
        MemberRequest.Register request = MemberRequest.Register.builder()
                .loginId("test1234")
                .password("test0000!!")
                .passwordCheck("test0000!!")
                .email("test1234@gmail.com")
                .nickName("tester")
                .birthDate(LocalDate.parse("2000-03-03"))
                .build();
        // 중복 확인 통과
        given(userRepository.existsByLoginId(any())).willReturn(false);
        given(userRepository.existsByEmail(any())).willReturn(false);


        // 비밀번호 암호화
        given(passwordEncoder.encode(anyString())).willReturn("hash_pw");

        User mockUser = User.builder()
                .loginId("test1234")
                .nickName("tester")
                .build();
        given(userRepository.save(any(User.class))).willReturn(mockUser);

        MemberResponse.Register response = userService.signup(request);
        // 서비스 실행 후 user 체크
        // user 낚아오기
        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(user.capture());

        // 가져온 객체 꺼내기
        User savedUser = user.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hash_pw");
        assertThat(response.nickName()).isEqualTo(request.nickName());
    }


}
