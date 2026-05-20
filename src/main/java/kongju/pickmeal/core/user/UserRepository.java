package kongju.pickmeal.core.user;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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

    List<User> findAllByFamilyId(Long familyId);
    boolean existsByIdAndFamilyId(Long id, Long familyId);
}
