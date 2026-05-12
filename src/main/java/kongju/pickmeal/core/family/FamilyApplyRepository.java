package kongju.pickmeal.core.family;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FamilyApplyRepository extends JpaRepository<JoinApply, Long> {

    @Query("SELECT COUNT(ja) > 0 " +
            "FROM JoinApply ja WHERE ja.userId = :userId AND " +
            "ja.status =:status AND " +
            "ja.familyId =:familyId"
    )
    boolean checkPendingApply(@Param("userId") Long userId, @Param("familyId") Long familyId, @Param("status") ApplyStatus status);
}
