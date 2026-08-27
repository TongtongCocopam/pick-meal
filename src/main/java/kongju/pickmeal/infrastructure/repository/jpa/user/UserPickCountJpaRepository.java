package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.user.UserPickCount;


public interface UserPickCountJpaRepository extends JpaRepository<UserPickCount, Long> {
    Optional<UserPickCount> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select upc
                from UserPickCount upc
                where upc.user = :user
            """)
    Optional<UserPickCount> findByUserForUpdate(@Param("user") User user);

    void deleteByUser(User user);

    void deleteAllByUser_Family(Family family);
}
