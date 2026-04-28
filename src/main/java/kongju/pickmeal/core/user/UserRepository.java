package kongju.pickmeal.core.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 유저 레포지토리
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 로그인할 아이디가 있는지 확인
     * @param loginId 찾을 아이디
     * @return 유저 반환
     */
    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
}
