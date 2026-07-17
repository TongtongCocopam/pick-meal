package kongju.pickmeal.application.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.user.repository.UserRepository;


@Component
@RequiredArgsConstructor
public class UserReader {
    private final UserRepository userRepository;

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
