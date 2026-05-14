package kongju.pickmeal.application.user;

import java.util.Objects;
import java.time.LocalDate;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserRepository;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.application.user.data.UserDto;
import kongju.pickmeal.common.exception.BusinessException;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\W]{8,16}$");

    /**
     * 회원가입 기능
     *
     * @param request 회원가입 정보
     * @return 닉네임 반환
     */
    public UserDto.SignupResponse signup(UserDto.SignupRequest request) {
        // 중복 이메일, 아이디 확인
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, request.loginId());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, request.email());
        }
        validateResiterRequest(request);
        // 비밀번호 해시 저장
        String password = passwordEncoder.encode(request.password());

        User user = User.builder()
                .loginId(request.loginId())
                .email(request.email())
                .password(password)
                .birthDate(request.birthDate())
                .nickName(request.nickName())
                .build();

        User savedUser = userRepository.save(user);

        return UserDto.SignupResponse.builder()
                .userId(savedUser.getId())
                .nickName(savedUser.getNickName())
                .build();
    }

    /**
     * 회원가입 데이터 유효성 검사하고 에러 처리
     * @param request 회원가입시 필요한 데이터
     */
    private void validateResiterRequest(UserDto.SignupRequest request) {
        // 아이디 길이 검사 (6~15자)
        if (request.loginId().length() < 6 || request.loginId().length() > 15) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "아이디는 6~15자 사이여야 합니다.");
        }

        // 비밀번호 일치 여부 검사
        if (!Objects.equals(request.password(), request.passwordCheck())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 비밀번호 정규식 검사 (영문, 숫자 포함 8~16자)
        if (!PASSWORD_PATTERN.matcher(request.password()).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호는 8~16자 사이여야 합니다.");
        }

        // 이메일 형식 검사 (간단한 정규식 또는 라이브러리 활용)
        if (!request.email().contains("@")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 생년월일 미래 날짜 여부 검사 (추가적인 비즈니스 로직)
        if (request.birthDate().isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

}
