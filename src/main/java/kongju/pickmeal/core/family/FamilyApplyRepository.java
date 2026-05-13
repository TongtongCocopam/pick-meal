package kongju.pickmeal.core.family;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.user.User;


public interface FamilyApplyRepository extends JpaRepository<JoinApply, Long> {

    @Query("SELECT COUNT(ja) > 0 " +
            "FROM JoinApply ja WHERE ja.user = :user AND " +
            "ja.status =:status AND " +
            "ja.familyId =:familyId"
    )
    boolean checkPendingApply(User user, Long familyId, ApplyStatus status);

    @Query("SELECT ja FROM JoinApply ja " +
            "JOIN FETCH ja.user " +
            "WHERE ja.familyId = :familyId AND " +
            "ja.status = :status"
    )
    List<JoinApply> findAllByFamilyIdAndStatus(Long familyId, ApplyStatus status);
}
