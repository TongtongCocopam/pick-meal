package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.type.UserMenuPickStatus;


public interface UserMenuPickJpaRepository extends JpaRepository<UserMenuPick, Long> {
    Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user);

    @Query("""
            select ump
            from UserMenuPick ump
            join fetch ump.menu
            where ump.id in :ids
            """)
    List<UserMenuPick> findAllByIdInFetchMenu(@Param("ids") List<Long> ids);


    @Query("""
                    select ump
                    from UserMenuPick  ump
                    join ump.user u
                    where u.family = :family
                    and ump.targetMonth = :targetMonthDate
                    and ump.status = :status
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UserMenuPick> findAllPendingForUpdate(
            @Param("family")Family family,
            @Param("targetMonthDate")LocalDate targetMonthDate,
            @Param("status")UserMenuPickStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select ump
                from UserMenuPick ump
                where ump.id in :ids
            """)
    List<UserMenuPick> findAllByIdInForUpdate(@Param("ids")List<Long> ids);

    void deleteAllByUser(User user);
}
