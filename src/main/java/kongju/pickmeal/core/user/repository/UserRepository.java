package kongju.pickmeal.core.user.repository;


import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;


public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    List<User> findAllByFamily(Family family);

    boolean existsByIdAndFamily(Long id, Family family);

    User save(User user);

    List<User> findAllFamily(Family family);

    void delete(User user);
}
